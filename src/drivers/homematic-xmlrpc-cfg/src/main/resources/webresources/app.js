/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function(){
	
	/*
	 * Dialog polyfill:
	 * https://github.com/GoogleChrome/dialog-polyfill
	 */
	let diagPromise = null;
	let infoAlertPromise = null;
	
	const loadLib = (src, cssOrJs) => {
		const script = document.createElement(cssOrJs ? "link" : "script");
		if (!cssOrJs) {
			script.type = "text/javascript";
			script.src = src;
		} else {
			script.href = src;
			script.rel = "stylesheet";
		}
		const p = new Promise((resolve, reject) => {
			script.onload = resolve;
			script.onerror = reject;
		});
		document.body.appendChild(script);
		return p;
	};

	const loadLibWithRetry = (src, altSrc, cssOrJs) => loadLib(src, cssOrJs).catch(() => loadLib(altSrc, cssOrJs));

	const loadDiagPolyfill = () => {
		if (!diagPromise) {
			diagPromise = loadLibWithRetry("https://cdnjs.cloudflare.com/ajax/libs/dialog-polyfill/0.4.10/dialog-polyfill.min.css", "lib/dialog-polyfill/dialog-polyfill.min.css", true).then(() => 
				loadLibWithRetry("https://cdnjs.cloudflare.com/ajax/libs/dialog-polyfill/0.4.10/dialog-polyfill.min.js", "lib/dialog-polyfill/dialog-polyfill.min.js"));
		}
		return diagPromise;
	};
	
	const registerDialog = diag => {
		if (!diag.showModal)
			return loadDiagPolyfill().then(() => {
				dialogPolyfill.registerDialog(diag);
				return diag;
			});
		else
			return Promise.resolve(diag);
	};
	//
	
	const SERVLET = "/ogemadrivers/homematicxmlrpc/cfgservlet";
	// FIXME instead of hard-coding we could query them when needed
	const THERMOSTAT_CONTROL_MODES = ["AUTO-MODE", "MANU-MODE", "PARTY-MODE", "BOOST-MODE"];
	// state
	let devices = null;
	let paramDetails = {};
	let initialized = false;
	
	const pushStateInternal = () => {
		if (!initialized)
			return;
		const search = new URLSearchParams(window.location.search);
		const hmInterface = document.getElementById("ifcSelect").value;
		const specialFunction = document.getElementById("specialFunctionSelect").value;
		if (hmInterface)
			search.set("interface", hmInterface);
		else
			search.delete("interface");
		if (specialFunction && specialFunction !== "empty")
			search.set("function", specialFunction);
		else
			search.delete("function");
		let text = window.location.pathname;
		if (!search.keys().next().done)
			text += "?"  +search.toString();
		history.pushState(null, null, text);
	};
	
	const showSpinner = (text) => {
		document.querySelector(".spinner").removeAttribute("data-state");
		const textEl = document.querySelector(".spinner-text");
		textEl.innerText = text;
		const length = Math.round(text.length * 0.222); 
		textEl.style.left = "calc(50% - " + length + "em)";
		textEl.removeAttribute("data-state");
	};
	
	const hideSpinner = () => {
		document.querySelector(".spinner").dataset.state="hidden";
		document.querySelector(".spinner-text").dataset.state="hidden";
	};
	
	const appendAll = (container, elements) => elements.forEach(el => container.appendChild(el));
	
	const clear = (el,excludedNodes) => {
		if (!excludedNodes) {
			while (el.firstChild) {
			    el.removeChild(el.firstChild);
			}
		} else {
			Array.from(el.children).forEach(c => {
				if (excludedNodes.indexOf(c.nodeName) < 0)
					el.removeChild(c);
			});
		}
	};
	
	const clearSelect = (select, excludedOptions) => {
		if (!excludedOptions) {
			while (select.firstChild) {
				select.removeChild(select.firstChild);
			}
		} else {
			Array.from(select.children).forEach(c => {
				if (excludedOptions.indexOf(c.value) < 0)
					select.removeChild(c);
			});
		}
	};
	
	const createOption = (value, text, select, selected) => {
		const opt = document.createElement("option");
		opt.value = value;
		opt.innerText = text;
		if (selected)
			opt.selected = "true";
		if (select)
			select.appendChild(opt);
	};
	
	const createEmptyOpt = select => createOption("empty", "", select);
	
	const arrayMatch = (arr1, arr2) => {
		for (let i=0; i<arr2.length; i++) {
			if (arr1.indexOf(arr2[i]) >= 0)
				return true;
		}
		return false;
	};
	
	const hasAnyRole = (address, roles, sourceOrTarget) => {
		const property = sourceOrTarget ? "LINK_SOURCE_ROLES" : "LINK_TARGET_ROLES";
		const dev = devices[address];
		if (!dev || !dev.hasOwnProperty(property))
			return false;
		return arrayMatch(roles, dev[property].split(" "));
	};
	
	/**
	 * @param text
	 * @param level
	 * 	0: error, 1: warning, 2: info
	 */
	const showAlert = (text, level) => {
		infoAlertPromise.then(diag => {
			const alert = diag.querySelector(".alert");
			alert.innerText = text;
			let state = "info";
			switch (level) {
			case 0:
				state = "error";
				break;
			case 1:
				state = "warning";
				break;
			default:
				break;
			}
			alert.dataset.state = state;
			diag.showModal();
		});
	};
	
	const createDialog = id => {
		const diag = document.createElement("dialog");
		if (id)
			diag.id = id;
		const closeBtn = document.createElement("span");
		closeBtn.innerHTML = "&#x274C;";
		closeBtn.classList.add("close-button");
		closeBtn.title = "Close";
		closeBtn.addEventListener("click", () => diag.close());
		diag.appendChild(closeBtn);
		return diag;
	};
	
	/**
	 * @param target
	 * @param params
	 * @param method: GET, POST, etc
	 * @param body: a String or undefined
	 * @param headers an object, or undefined
	 * @param text text to be shown in spinner while loading; if this is undefined then the spinner will not be shown
	 */
	const send = (method, target, text, params, body, headers) => {
		const fetchParams = {
			method: method,
			credentials: "same-origin"
		};
		if (!headers)
			headers = {};
		headers.Accept = "application/json";
		fetchParams.headers = headers;
		if (body) {
			fetchParams.body = JSON.stringify(body);
			headers.ContentType = "application/json";
		}
		if (!params)
			params = new URLSearchParams();
		params.set("user", otusr);
		params.set("pw", otpwd);
		params.set("target", target);
		if ("interfaces" !== target) {
			const ifc = document.getElementById("ifcSelect").value;
			params.set("interface", ifc);
		}
		const address = SERVLET + "?" + params.toString();
		console.log("NOW sending request to ",address);
		if (text)
			showSpinner(text);
		const promise = fetch(address, fetchParams)
			.then(response => {
				if (!response.ok)
					return Promise.reject("Request for " + target + " failed: "  + response.status + ", " + response.statusText);
				return response.json();
			});
		promise.finally(hideSpinner);
		if (text)
			promise.catch(error => showAlert(error, 0));
		return promise;
	};
	
	const newLabel = (text, fragment) => {
		const lab = document.createElement("span");
		if (text)
			lab.innerText = text;
		fragment.appendChild(lab);
		return lab;
	}

	const sortGrid = (grid, col, upOrDown) => {
		const rows = Array.from(grid.querySelectorAll(":scope>div"))
			.filter(r => r.style.display === "contents" || r.style.display === "none");
		const rowsByContent = rows
			.filter(r => r.children.length > col)
			.map(r => [r.children[col].innerText, r]);
		if (rowsByContent.length === 0)
			return;
		const sorted = [[rowsByContent[0][0].toLowerCase(), rowsByContent[0][1]]];
		for (let i=1; i<rowsByContent.length; i++) {
			const arr = rowsByContent[i];
			const text = arr[0].toLowerCase();
			let found = false;
			for (let j=0; j<i; j++) {
				if ((upOrDown && text < sorted[j][0]) || (!upOrDown && text > sorted[j][0])) {
					const el = grid.insertBefore(arr[1], sorted[j][1]);
					sorted.splice(j, 0, [text, el]); // Array.splice(j, 0, x) inserts x at position j in the array
					found = true;
					break;
				}
			}
			if (!found)
				sorted.push([text, arr[1]]);
		}
	};
	
	const toggleParamsDialog = (showOrHide, channel, parameter, type, params) => {
		const diag = document.querySelector("#paramsDiag");
		if (!showOrHide) {
			diag.close();
			return;
		}
		const descrParams = new URLSearchParams();
		descrParams.set("address", channel);
		descrParams.set("param", parameter);
		send("GET", "paramdescr", undefined, descrParams).then(resp => paramDetails = resp);
		diag.querySelector("#diagChannel").innerText = channel;
		diag.querySelector("#diagParam").innerText = parameter;
		diag.querySelector("#diagType").innerText = parameter;
		const grid = diag.querySelector(".params-grid");
		Array.from(grid.querySelectorAll(":scope>*"))
			.filter(e => e.nodeName !== "H4")
			.forEach(e => e.remove());
		const frag = document.createDocumentFragment();
		const keys = Object.keys(params).sort();
		keys.forEach(p => {
			const container = document.createElement("div");
			container.dataset.key = p;
			container.style.display = "contents";
			newLabel(p, container);
			const value = document.createElement("input");
			value.type = "input";
			value.value = params[p];
			container.appendChild(value);
			const submit = document.createElement("input");
			submit.type = "button";
			submit.value = "Submit";
			const currentParam = p;
			submit.addEventListener("click", () => {
				const body = {
					channel: channel,
					paramset_key: parameter,
					key: currentParam,
					value: value.value
				}; 
				send("POST", "setvalue", "Setting parameter...", undefined, body)
					.then(resp => {
						value.value = resp[currentParam];
					}).catch(e => {
						value.value = params[currentParam];
						console.log("Error ", e); // TODO update params to old values
					}); 
			});
			container.appendChild(submit);
			const detailsContainer = document.createElement("div");
			const showDetails = document.createElement("input");
			showDetails.type = "button";
			showDetails.value = "Show details"
			showDetails.addEventListener("click", event => {
				let area = detailsContainer.querySelector("textarea");
				if (area !== null) {
					showDetails.value = "Show details";
					area.remove();
				} else if (paramDetails.hasOwnProperty(currentParam)) {
					showDetails.value = "Hide details";
					const obj = paramDetails[currentParam];
					area = document.createElement("textarea");
					area.value = JSON.stringify(obj, undefined, 4);
					detailsContainer.appendChild(area);
				}
			});
			detailsContainer.appendChild(showDetails);
			container.appendChild(detailsContainer);
			container.appendChild(document.createElement("div"));
			frag.appendChild(container);
		});
		grid.appendChild(frag);
		diag.showModal();
	};

	const isVisible = (row, filterArray) => {
		for (let i=0; i<filterArray.length; i++) {
			if (filterArray[i] && row.children[i].innerText.toLowerCase().indexOf(filterArray[i]) < 0)
				return false;
		}
		return true;
	};
	
	const getFilterArray = () => {
		const address = document.getElementById("addressFilter").value.trim().toLowerCase();
		const devType = document.getElementById("devTypeFilter").value.trim().toLowerCase();
		const room = document.getElementById("roomFilter").value.trim().toLowerCase();
		const type = document.getElementById("typeFilter").value.trim().toLowerCase();
		return [address, devType, room, type];
	};
	
	const filter = () => {
		const filterArray = getFilterArray();
		Array.from(document.querySelector("#devices").querySelectorAll(":scope>[data-address]"))
			.forEach(row => row.style.display = isVisible(row, filterArray) ? "contents" : "none");
	};
	
	const showLinkDialog = address => {
		const diag = document.getElementById("linkDiag");
		const roles0 = diag.querySelector("#addLinkRoles0");
		const sender0 = diag.querySelector("#addLinkSender0");
		const rec0 = diag.querySelector("#addLinkReceiver0");
		const roles1 = diag.querySelector("#addLinkRoles1");
		const sender1 = diag.querySelector("#addLinkSender1");
		const rec1 = diag.querySelector("#addLinkReceiver1");
		clearSelect(roles0);
		clearSelect(sender0);
		clearSelect(rec0, "empty");
		clearSelect(roles1);
		clearSelect(sender1, "empty");
		clearSelect(rec1);
		const dev = devices[address];
		if (!dev)
			return;
		const senderRoles = dev.LINK_SOURCE_ROLES.split(" ").filter(role => role.length > 0);
		const receiverRoles = dev.LINK_TARGET_ROLES.split(" ").filter(role => role.length > 0);
		const matchingReceivers = senderRoles.length === 0 ? [] : 
			Object.keys(devices)
				.filter(add => hasAnyRole(add, senderRoles, false));
		const matchingSenders = receiverRoles.length === 0 ? [] : 
			Object.keys(devices)
				.filter(add => hasAnyRole(add, receiverRoles, true));
//				.filter(add => devices[add].hasOwnProperty("LINK_SOURCE_ROLES"))
//				.filter(add => arrayMatch(receiverRoles, devices[add].LINK_SOURCE_ROLES.split(" ")))
		const uniqueSender = matchingReceivers.length > 0 ? createOption(address, address, sender0) : null;
		const uniqueReceiver = matchingSenders.length > 0 ? createOption(address, address, rec1) : null;
		const roles0Frag = document.createDocumentFragment();
		const roles1Frag = document.createDocumentFragment();
		const recFrag = document.createDocumentFragment();
		const senderFrag = document.createDocumentFragment();
		senderRoles.forEach(role => createOption(role, role, roles0Frag, true));
		receiverRoles.forEach(role => createOption(role, role, roles1Frag, true));
		matchingReceivers
			.filter(rec => !dev.hasOwnProperty("senderLinks") || !(rec in dev.senderLinks))
			.forEach(rec => createOption(rec, rec, recFrag));
		matchingSenders
			.filter(rec => !dev.hasOwnProperty("receiverLinks") || !(rec in dev.receiverLinks))
			.forEach(rec => createOption(rec, rec, senderFrag));
		roles0.appendChild(roles0Frag);
		roles1.appendChild(roles1Frag);
		rec0.appendChild(recFrag);
		sender1.appendChild(senderFrag);
		rec0.dispatchEvent(new Event("change"));
		sender1.dispatchEvent(new Event("change"));
		diag.showModal();
	};
	
	const showDeleteLink = (sender, receiver) => {
		const diag = document.getElementById("removeLinkDialog");
		diag.querySelector("#deleteLinkSender").innerText = sender;
		diag.querySelector("#deleteLinkReceiver").innerText = receiver;
		diag.showModal();
	};
	
	const createRow = (deviceObj, parentElement, filterArray) => {
		const frag = document.createDocumentFragment();
		const addr = deviceObj.ADDRESS;
		const container = document.createElement("div");
		container.dataset.address = addr;
		newLabel(addr, container);
		newLabel(deviceObj.restype, container);
		newLabel(deviceObj.room, container);
		newLabel(deviceObj.TYPE, container);
		// must be called after adding the filterable labels!
		container.style.display = isVisible(container, filterArray) ? "contents" : "none";
		const nrChannels = !deviceObj.hasOwnProperty("CHILDREN") ? 0 : deviceObj.CHILDREN.length;
		const channelsContainer = document.createElement("div");
		newLabel(nrChannels, channelsContainer);
		if (nrChannels > 0) {
			const channels = document.createElement("input");
			channels.type = "button";
			channels.value = "Show channels";
			channels.style["margin-left"] = "1em";
			channels.dataset.state = "0"; // sub grid hidden; "1": visible
			channelsContainer.appendChild(channels);
			channels.addEventListener("click", evt => {
				const visible = channels.dataset.state === "1";
				let subcontainer = container.querySelector(":scope>div.subgrid-container");
				if (!visible) { // TODO create sub grid
					if (subcontainer === null) {
						subcontainer = document.createElement("div");
						subcontainer.classList.add("subgrid-container");
						for (channelAddr in devices) {
							if (channelAddr.startsWith(addr + ":"))
								createRow(devices[channelAddr], subcontainer, filterArray);
						}
						container.appendChild(subcontainer);
					}
				} 
				
				channels.dataset.state = visible ? "0" : "1";
				channels.value = visible ? "Show channels" : "Hide channels";
				subcontainer.dataset.state = visible ? "0" : "1";
			});
		}
		container.appendChild(channelsContainer);
		const paramsContainer = document.createElement("div");
		let hasLinks = false;
		if (deviceObj.hasOwnProperty("PARAMSETS")) {
			deviceObj.PARAMSETS.forEach(p => {
				if (p === "LINK")
					hasLinks  =true;
				const span = document.createElement("span");
				span.innerText = p;
				span.classList.add("parameter-field");
				span.classList.add("trigger-text");
				span.title = "Click to display and edit parameter values";
				span.addEventListener("click", evnt => {
					params = new URLSearchParams();
					params.set("param", p);
					params.set("address", addr);
					send("GET", "paramread", "Loading parameters...", params).then(resp => {
						toggleParamsDialog(true, addr, p, deviceObj.TYPE, resp);
					});
				});
				paramsContainer.appendChild(span);
			})
		}
		container.appendChild(paramsContainer);
		const linkCt = document.createElement("div");
		const links = document.createElement("ul");
		linkCt.appendChild(links);
		links.classList.add("link-list");
		if (deviceObj.hasOwnProperty("receiverLinks")) {
			Object.keys(deviceObj.receiverLinks).forEach(sender => {
				const link = deviceObj.receiverLinks[sender];
				const li = document.createElement("li");
				li.innerText = "Sender: " + sender;
				if (link.NAME) {
					const n = " (" + link.NAME + ")";
					li.innerText = li.innerText + n;
				}
				if (link.DESCRIPTION) {
					li.title = link.DESCRIPTION;
				}
				const remove = document.createElement("input");
				remove.type = "button";
				remove.value = "Remove";
				remove.classList.add("margin-left");
				const currentSender = sender;
				remove.addEventListener("click", event => showDeleteLink(currentSender, addr));
				li.appendChild(remove);
				links.appendChild(li);
			});
		}
		if (deviceObj.hasOwnProperty("senderLinks")) {
			Object.keys(deviceObj.senderLinks).forEach(rec => {
				const link = deviceObj.senderLinks[rec];
				const li = document.createElement("li");
				li.innerText = "Receiver: " + rec;
				if (link.NAME) {
					const n = " (" + link.NAME + ")";
					li.innerText = li.innerText + n;
				}
				if (link.DESCRIPTION) {
					li.title = link.DESCRIPTION;
				}
				const remove = document.createElement("input");
				remove.type = "button";
				remove.value = "Remove";
				remove.classList.add("margin-left");
				const currentRec = rec;
				remove.addEventListener("click", event => showDeleteLink(addr, currentRec));
				li.appendChild(remove);
				links.appendChild(li);
			});
		}
		if (hasLinks) {
			const ct = document.createElement("div")
			const addlink = document.createElement("input");
			addlink.type = "button";
			addlink.value = "Add link";
			ct.appendChild(addlink);
			container.appendChild(ct);
			addlink.addEventListener("click", () => showLinkDialog(addr));
		} else {
			container.appendChild(document.createElement("div"));
		}
		container.appendChild(linkCt);
		const empty = document.createElement("div");
		container.appendChild(empty);
		frag.appendChild(container);
		parentElement.appendChild(frag);
	};
	
	const updateDevices = () => {
		const el = document.getElementById("devices");
		clear(el, ["H3"]);
		return send("GET", "devices", "Loading devices...").then(resp => {
			return send("GET", "links", "Loading link infos...").then(links => {
				devices = resp;
				links.forEach(link => {
					const sender = link.SENDER;
					const receiver = link.RECEIVER;
					const s = devices[sender];
					if (s) {
						if (!s.hasOwnProperty("senderLinks")) 
							s.senderLinks = {};
						s.senderLinks[receiver] = link;
					}
					const r = devices[receiver];
					if (r) {
						if (!r.hasOwnProperty("receiverLinks")) 
							r.receiverLinks = {};
						r.receiverLinks[sender] = link;
					}
				});
				
				const newDevices = new Map(Object.entries(devices)
					.filter(d => !d[1].PARENT)
					.filter(d => el.querySelector(":scope>[data-address='" + d[1].ADDRESS + "']") === null));
				const data = Array.from(newDevices, ([key,value]) => key);
				/* // we already cleared the whole element
				Array.from(el.querySelectorAll(":scope>[data-address]"))
					.filter(e => data.indexOf(e.dataset.address) < 0)
					.forEach(e => e.remove());
				*/
				newDevices.forEach(d => createRow(d, el, getFilterArray()));
			});
		});
	};

	// TODO fallback for case that <dialog> does not exist
	const appendParamsDialog = () => {
		const diag = createDialog("paramsDiag");
		const diagFrag = document.createDocumentFragment();
		newLabel("Channel: ", diagFrag);
		const diagChan = document.createElement("span");
		diagChan.id = "diagChannel";
		diagFrag.appendChild(diagChan);
		diagFrag.appendChild(document.createElement("br")); // TODO structure
		newLabel("Parameter set: ", diagFrag);
		const diagParam = document.createElement("span");
		diagParam.id = "diagParam";
		diagFrag.appendChild(diagParam);
		diagFrag.appendChild(document.createElement("br")); // TODO structure
		newLabel("Type: ", diagFrag);
		const diagType = document.createElement("span");
		diagType.id = "diagType";
		diagFrag.appendChild(diagType);
		diagFrag.appendChild(document.createElement("br"));
		
		const filterRow = document.createElement("div");
		filterRow.classList.add("filter-row");
		newLabel("Filter keys:", filterRow);
		const keyFilter = document.createElement("input");
		keyFilter.type = "input";
		filterRow.appendChild(keyFilter);
		diagFrag.appendChild(filterRow);
		
		const diagGrid = document.createElement("div");
		diagGrid.classList.add("params-grid");
		diagGrid.classList.add("grid-sortable");
		const hKey = document.createElement("h4");
		hKey.innerText = "Key";
		hKey.classList.add("col-sortable");
		const hValue = document.createElement("h4");
		hValue.innerText = "Value";
		const hDetails = document.createElement("h4");
		hDetails.innerText = "Details";
		diagGrid.appendChild(hKey);
		diagGrid.appendChild(hValue);
		diagGrid.appendChild(document.createElement("h4")); // submit
		diagGrid.appendChild(hDetails);
		diagGrid.appendChild(document.createElement("h4"));
		
		diagFrag.appendChild(diagGrid);
		diag.appendChild(diagFrag);
		document.body.appendChild(diag);
		
		keyFilter.addEventListener("change", event => {
			const value = event.currentTarget.value.trim().toLowerCase();
			Array.from(diagGrid.querySelectorAll(":scope>[data-key]"))
				.forEach(row => row.style.display = row.dataset.key.toLowerCase().indexOf(value) >= 0 ? "contents" : "none");
		});
		registerDialog(diag);
	};

	
	const appendLinkDialog = () => {
		const diag = createDialog("linkDiag");
		const title = document.createElement("h2");
		title.innerText = "Add link";
		diag.appendChild(title);
		
		const metaGrid = document.createElement("div");
		metaGrid.classList.add("addlinkmeta-grid");
		newLabel("Link name", metaGrid);
		const nameInput = document.createElement("input");
		nameInput.type = "input";
		metaGrid.appendChild(nameInput);
		newLabel("", metaGrid);
		newLabel("Link description", metaGrid);
		const descInput = document.createElement("input");
		descInput.type = "input";
		metaGrid.appendChild(descInput);
		newLabel("", metaGrid);
		diag.appendChild(metaGrid);
		
		const addLink = event => {
			const btn = event.currentTarget;
			const receiverSelect = btn.parentElement.previousElementSibling;
			const sender = receiverSelect.previousElementSibling.querySelector("select").value;
			const receiver = receiverSelect.querySelector("select").value;
			if (sender === "empty" || receiver === "empty")
				return;
			const body = {
				sender: sender,
				receiver: receiver
			};
			const name = nameInput.value.trim();
			if (name)
				body.name = name;
			const desc = descInput.value.trim();
			if (desc)
				body.description = desc;
			// TODO report result! 
			const p = send("POST", "addlink", undefined, undefined, body);
			p.then(updateDevices);
			p.finally(() => diag.close());
		};

		const grid = document.createElement("div");
		grid.classList.add("addlink-grid");
		
		const headRoles = document.createElement("h4");
		const headSender = document.createElement("h4");
		const headRec = document.createElement("h4");
		const headAdd = document.createElement("h4");
		headRoles.innerText = "Roles";
		headSender.innerText = "Sender";
		headRec.innerText = "Receiver";
		headAdd.innerText = "Submit";
		
		const createNestedSelect = id => {
			const div = document.createElement("div");
			const select = document.createElement("select");
			select.id = id;
			div.appendChild(select);
			return div;
		};
		
		const roles0 = document.createElement("select");
		roles0.multiple = true;
		roles0.id = "addLinkRoles0";
		const sender0Div = createNestedSelect("addLinkSender0");
		const sender0 = sender0Div.querySelector("select");
		const rec0Div = createNestedSelect("addLinkReceiver0");
		const rec0 = rec0Div.querySelector("select");
		createEmptyOpt(rec0);
		const addDiv0 = document.createElement("div");
		const add0 = document.createElement("input");
		add0.type = "button";
		add0.value = "Add link";
		add0.addEventListener("click", addLink);
		addDiv0.appendChild(add0);
		roles0.addEventListener("change", event => {
			const values = Array.from(event.currentTarget.selectedOptions).map(o => o.value);
			Array.from(rec0.options)
				.filter(o => o.value !== "empty")
				.forEach(o => hasAnyRole(o.value, values, false) ? o.style.removeProperty("display") : o.style.display = "none");
			if (Array.from(rec0.selectedOptions)
				.filter(o => o.style.display === "none").length > 0)
				rec0.selectedIndex = 0;
			rec0.dispatchEvent(new Event("change"));
		});
		rec0.addEventListener("change", event => event.currentTarget.value === "empty" ? add0.disabled = "true" : add0.removeAttribute("disabled"));
		
		const roles1 = document.createElement("select");
		roles1.multiple = true;
		roles1.id = "addLinkRoles1";
		
		const sender1Div = createNestedSelect("addLinkSender1");
		const sender1 = sender1Div.querySelector("select");
		const rec1Div = createNestedSelect("addLinkReceiver1");
		const rec1 = rec1Div.querySelector("select");
		createEmptyOpt(sender1);
		const addDiv1 = document.createElement("div");
		const add1 = document.createElement("input");
		add1.type = "button";
		add1.value = "Add link";
		add1.addEventListener("click", addLink);
		addDiv1.appendChild(add1);
		roles1.addEventListener("change", event => {
			const values = Array.from(event.currentTarget.selectedOptions).map(o => o.value);
			Array.from(sender1.options)
				.filter(o => o.value !== "empty")
				.forEach(o => hasAnyRole(o.value, values, true) ? o.style.removeProperty("display") : o.style.display = "none");
			if (Array.from(sender1.selectedOptions)
					.filter(o => o.style.display === "none").length > 0)
					sender1.selectedIndex = 0;
			sender1.dispatchEvent(new Event("change"));
		});
		sender1.addEventListener("change", event => event.currentTarget.value === "empty" ? add1.disabled = "true" : add1.removeAttribute("disabled"));
		
		grid.appendChild(headRoles);
		grid.appendChild(headSender);
		grid.appendChild(headRec);
		grid.appendChild(headAdd);
		grid.appendChild(roles0);
		grid.appendChild(sender0Div);
		grid.appendChild(rec0Div);
		grid.appendChild(addDiv0);
		grid.appendChild(roles1);
		grid.appendChild(sender1Div);
		grid.appendChild(rec1Div);
		grid.appendChild(addDiv1);
		diag.appendChild(grid);
		document.body.appendChild(diag);
		registerDialog(diag);
	};
	
	const appendRemoveLinkDialog = () => {
		const diag = createDialog("removeLinkDialog");
		diag.classList.add("confirm-popup");
		const div = document.createElement("div");
		const span0 = document.createElement("span");
		span0.innerText = "Do you really want to delete the link between ";
		div.appendChild(span0);
		const spanSender = document.createElement("span");
		spanSender.id = "deleteLinkSender";
		div.appendChild(spanSender);
		const span1 = document.createElement("span");
		span1.innerText = " and ";
		div.appendChild(span1);
		const spanReceiver = document.createElement("span");
		spanReceiver.id = "deleteLinkReceiver";
		div.appendChild(spanReceiver);
		const btnCancel = document.createElement("input");
		btnCancel.type = "button";
		btnCancel.value = "Cancel";
		const btnConfirm = document.createElement("input");
		btnConfirm.type = "button";
		btnConfirm.value = "Confirm";
		btnCancel.addEventListener("click", () => diag.close());
		btnConfirm.addEventListener("click", () => {
			const sender = spanSender.innerText;
			const receiver = spanReceiver.innerText;
			if (!sender || !receiver)
				return;
			const params = {
				sender: sender,
				receiver: receiver
			}
			const p = send("DELETE", "link", undefined, undefined, params);
			p.then(updateDevices);
			p.finally(() => diag.close());
		});
		const btnRow = document.createElement("div");
		btnRow.classList.add("btn-row");
		btnRow.appendChild(btnCancel);
		btnRow.appendChild(btnConfirm);
		diag.appendChild(div);
		diag.appendChild(btnRow);
		document.body.appendChild(diag);
		registerDialog(diag);
	};
	
	const setFilters = () => {
		document.getElementById("addressFilter").addEventListener("change", filter);
		document.getElementById("devTypeFilter").addEventListener("change", filter);
		document.getElementById("roomFilter").addEventListener("change", filter);
		document.getElementById("typeFilter").addEventListener("change", filter);
		document.getElementById("showFilters").addEventListener("click", event => {
			event.currentTarget.dataset.state = "hidden";
			document.getElementById("filtersSet").dataset.state = "visible";
		});
		document.getElementById("filtersSet").querySelector("legend").addEventListener("click", event => {
			document.getElementById("filtersSet").removeAttribute("data-state");
			document.getElementById("showFilters").removeAttribute("data-state");
		});
		
	};
	
	const initSorting = () => {
		Array.from(document.querySelectorAll(".col-sortable"))
			.filter(el => el.parentNode.classList.contains("grid-sortable"))
			.forEach(el => {
				const parent = el.parentNode;
				const idx = Array.from(el.parentNode.children).indexOf(el);
				if (idx < 0) // should not happen
					return;
				el.addEventListener("click", event => {
					const t = event.currentTarget;
					const state = t.dataset.hasOwnProperty("state") && t.dataset.state === "+";
					sortGrid(parent, idx, !state);
					t.dataset.state = state ? "-" : "+";
				});
			});
	};
	
	const getDevicesByType = ids => Object.keys(devices).map(d => devices[d])
		.filter(d => ids.find(id => d.TYPE.startsWith(id)) !== undefined); 
	
	// FIXME what about Hm IP?
	const getThermostats = () => getDevicesByType(["HM-CC-RT-DN"]); 
	const getTHSensors = () => getDevicesByType(["HM-WDS40-TH-","HM-WDS10-TH-"]);
	const getPowerSensors = () => getDevicesByType(["HM-ES-PMSw"]); 
	
	const newDeviceLabel = (device, addDevType, container) => {
		let text = device.ADDRESS;
		const hasRoom = device.room ? true : false;
		const hasType = addDevType && device.restype ? true: false;
		if (hasRoom || hasType) {
			text += " (";
			if (hasRoom)
				text += device.room;
			if (hasType) {
				if (hasRoom)
					text += ", ";
				text += device.restype;
			}
			text += ")";
		}
		newLabel(text, container);
	};
	
	const updateThermostats = () => {
		const grid = document.getElementById("thermostatsGrid");
		clear(grid, ["SPAN"]);
		const thermos = getThermostats();
		const frag = document.createDocumentFragment();
		const promises = [];
		thermos.forEach(thermo => {
			const params = new URLSearchParams();
			params.set("address", thermo.ADDRESS + ":4");
			params.set("key", "CONTROL_MODE");
			const currentThermo = thermo;
			promises.push(send("GET", "readValue", undefined, params).then(respMode => {
				params.set("key", "SET_TEMPERATURE");
				return send("GET", "readValue", undefined, params).then(respSetpoint => {
					params.set("address", thermo.ADDRESS);
					params.set("param", "MASTER");
					return send("GET", "paramread", undefined, params).then(respLock => {
						const div = document.createElement("div");
						div.style.display = "contents";
						let text = currentThermo.ADDRESS;
						if (currentThermo.room)
							text = text + " (" + currentThermo.room + ")";
						newLabel(text, div);
						const modeSelect = document.createElement("select");
						for (let i=0; i < THERMOSTAT_CONTROL_MODES.length; i++) {
							const opt = document.createElement("option");
							opt.value = i;
							opt.innerText = THERMOSTAT_CONTROL_MODES[i];
							opt.selected = respMode.result === i; // TODO check
							modeSelect.appendChild(opt);
						}
						modeSelect.addEventListener("change", event => {
							const idx = event.currentTarget.selectedIndex; // idx  = mode
							const body = {
								"channel":  currentThermo.ADDRESS + ":4",
								"key" : "CONTROL_MODE",
								"value": THERMOSTAT_CONTROL_MODES[idx],
								"paramset_key" : "VALUES"
							};
							params.set("address", currentThermo.ADDRESS + ":4");
							params.set("key", "CONTROL_MODE");
							send("POST", "setvalue", undefined, undefined, body)
								.finally(() => send("GET", "readValue", undefined, params).then(resp => modeSelect.selectedIndex = resp.result));
						});
						const selectWrapper = document.createElement("div");
						selectWrapper.appendChild(modeSelect);
						div.appendChild(selectWrapper);
						const lockCheck = document.createElement("input");
						lockCheck.type = "checkbox";
						const checked = respLock.BUTTON_LOCK === "true"; // TODO check
						lockCheck.checked = checked;
						lockCheck.addEventListener("change", event => {
							const checked2 = event.currentTarget.checked; 
							const body = {
									"channel":  currentThermo.ADDRESS,
									"key" : "BUTTON_LOCK",
									"value": checked2 + "",
									"paramset_key" : "MASTER"
							};
							const params2 = new URLSearchParams();
							params2.set("address", thermo.ADDRESS);
							params2.set("param", "MASTER");
							send("POST", "setparam", undefined, undefined, body)
								.finally(() => send("GET", "paramread", undefined, params2).then(resp => lockCheck.checked = resp.BUTTON_LOCK.toLowerCase() === "true"));
						});
						div.appendChild(lockCheck);
						/*
						const isLocked = respLock.BUTTON_LOCK.toLowerCase() === "true";
						const span = document.createElement("span");
						span.innerHTML = isLocked ? "&#10004;" : "&#10060;";
						div.appendChild(span);
						*/
						const setp = document.createElement("input");
						setp.type = "input";
						setp.value = respSetpoint.result + "°C";
						setp.addEventListener("change", event => {
							const v = parseFloat(event.currentTarget.value.replace("°","").replace("C", "").trim());
							const valid = !isNaN(v);
							const body = {
								channel : currentThermo.ADDRESS + ":4",
								paramset_key : "VALUES",
								key : "SET_TEMPERATURE",
								value : v + ""
							};
							const params2 = new URLSearchParams();
							params2.set("address", currentThermo.ADDRESS + ":4");
							params2.set("key", "SET_TEMPERATURE");
							const p2 = !valid ? Promise.resolve(null) :
								send("POST", "setvalue", undefined, undefined, body);
							p2.finally(() => send("GET", "readvalue", undefined, params2).then(res => setp.value = res.result + "°C"));
						});
						div.appendChild(setp);
						newLabel(undefined, div);
						frag.appendChild(div);
					});
				});
			}));
		});
		Promise.all(promises).then(() => grid.appendChild(frag));
	};
	
	const updatePower = () => {
		const allOff = document.getElementById("powerAllOff");
		allOff.removeAttribute("data-state"); // hide
		const grid = document.getElementById("powerGrid");
		clear(grid, ["SPAN"]);
		const frag = document.createDocumentFragment();
		const promises = [];
		const sensors = getPowerSensors();
		sensors.forEach(sensor => {
			const params = new URLSearchParams();
			params.set("address", sensor.ADDRESS + ":1");
			params.set("key", "STATE");
			const currentSensor = sensor;
			promises.push(send("GET", "readvalue", undefined, params).then(respSw => {
				params.set("address", currentSensor.ADDRESS + ":2");
				params.set("param", "VALUES");
				return send("GET", "paramread", undefined, params).then(resp => {
					const div = document.createElement("div");
					div.style.display = "contents";
					let text = currentSensor.ADDRESS;
					if (currentSensor.room)
						text = text + "(" + currentSensor.room + ")"; 
					newLabel(text, div);
					const check = document.createElement("input");
					check.type = "checkbox";
					check.checked = respSw.result.toLowerCase() === "true";
					check.addEventListener("change", event => {
						const checked = event.currentTarget.checked;
						const postParams = {
								channel : currentSensor.ADDRESS + ":1",
								paramset_key : "VALUES",
								key : "STATE",
								value : checked ? "true" : "false"
							};
							send("POST", "setvalue", undefined, undefined, postParams)
								.finally(updatePower);
					});
					div.appendChild(check);
					newLabel(resp.POWER + " W", div); // TODO check units
					newLabel(resp.ENERGY_COUNTER + " Wh", div); 
					newLabel(resp.VOLTAGE + " V", div); 
					newLabel(resp.CURRENT + " mA", div); 
					newLabel(resp.FREQUENCY + " Hz", div); 
					newLabel(undefined, div);
					frag.appendChild(div);
				});
			}));
		});
		Promise.all(promises).then(() => {
			allOff.dataset.state = "visible";
			grid.appendChild(frag);
		});
	};
	
	const getMaintenanceChannel = address => {
		if (address.indexOf(":") >= 0)
			return null;
		return Object.keys(devices)
			.filter(d => d.indexOf(address + ":") >= 0)
			.map(d => devices[d])
			.find(d => d.TYPE === "MAINTENANCE")
	};
	
	const getDevicesWithMaintenance = () => {
		return Object.keys(devices)
			.map(d => devices[d])
			.filter(d => d.TYPE === "MAINTENANCE")
			.map(d => d.ADDRESS.substring(0, d.ADDRESS.indexOf(":")))
			.filter(add => add && devices.hasOwnProperty(add))
			.map(add => devices[add])
	};
	
	const updateUnreach = () => {
		const grid = document.getElementById("unreachGrid");
		clear(grid, ["SPAN"]);
		const frag = document.createDocumentFragment();
		const promises = [];
		const devs = getDevicesWithMaintenance();
		devs.forEach(d => {
			const params = new URLSearchParams();
			params.set("address", d.ADDRESS + ":0"); // FIXME hardcoded
			params.set("key", "UNREACH"); // FIXME check param key for switch state
			const currentDevice = d;
			promises.push(send("GET", "readvalue", undefined, params).then(respUnreach => {
				params.set("key", "STICKY_UNREACH");
				return send("GET", "readvalue", undefined, params)
						.catch(ignore => null).then(respSticky => { // IP devices do not seem to support STICKY_UNREACH
					
					const div = document.createElement("div");
					div.style.display = "contents";
					newDeviceLabel(currentDevice, true, div);
					newLabel(respUnreach.result, div);
					const stickyText = respSticky ? respSticky.result : undefined;
					const stickyLab = newLabel(stickyText, div);
					if (respSticky && respUnreach.result && respSticky.result && 
							respSticky.result.toLowerCase() === "true" && respUnreach.result.toLowerCase() !== "true") {
						const btn = document.createElement("input");
						btn.type = "button";
						btn.value = "Reset sticky unreach";
						btn.title = "Set STICKY_UNREACH parameter to false, until next UNREACH event occurs.";
						btn.addEventListener("click", event => {
							const postParams = {
								channel : currentDevice.ADDRESS + ":0",
								paramset_key : "VALUES",
								key : "STICKY_UNREACH",
								value : "false"
							};
							const currentTarget = event.currentTarget;
							send("POST", "setvalue", undefined, undefined, postParams)
								.finally(() => {
									send("GET", "readvalue", undefined, params)
										.then(res => {
											stickyLab.innerText = res.result;
											if (!res.result || res.result === "false")
												currentTarget.remove();
										});
								});
						});
						div.appendChild(btn);
					} else {
						newLabel(undefined, div);
					}
					newLabel(undefined, div);
					frag.appendChild(div);
				});
			}).catch(e => console.log("error",e)));
		});
		Promise.all(promises).then(() => grid.appendChild(frag));
	};
	
	const updateBattery = () => {
		const grid = document.getElementById("batteryGrid");
		clear(grid, ["SPAN"]);
		const frag = document.createDocumentFragment();
		const promises = [];
		const devs = getDevicesWithMaintenance();
		devs.forEach(d => {
			const key = d.TYPE.startsWith("HMIP") ? "LOW_BAT" : "LOWBAT";// FIXME hardcoded
			const params = new URLSearchParams();
			params.set("address", d.ADDRESS + ":0"); // FIXME hardcoded
			params.set("key", key); 
			const currentDevice = d;
			promises.push(send("GET", "readvalue", undefined, params).then(respLow => {
				const hasVoltage = devices.hasOwnProperty(currentDevice.ADDRESS + ":4") && devices[currentDevice.ADDRESS + ":4"].TYPE === "CLIMATECONTROL_RT_TRANSCEIVER";
				if (hasVoltage) {
					params.set("address", currentDevice.ADDRESS + ":4");
					params.set("key", "BATTERY_STATE");
				}
				//TODO LOW_BAT_LIMIT may exist even if state is not reported (IP DoorWindowSensor)
				const p = hasVoltage ? 
						send("GET", "readvalue", undefined, params).then(respState => {
							params.set("address", currentDevice.ADDRESS);
							params.set("param", "MASTER");
							return send("GET", "paramread", undefined, params).then(respLimit => {
								return send("GET", "paramdescr", undefined, params).then(respDefault => [respState.result, respLimit.LOW_BAT_LIMIT, respDefault.LOW_BAT_LIMIT.DEFAULT]);
							});
						})
					: Promise.resolve(null);
				return p.then(arr => {
					const div = document.createElement("div");
					div.style.display = "contents";
					newDeviceLabel(currentDevice, true, div);
					newLabel(respLow.result, div);
					if (arr) {
						newLabel(arr[0] + " V", div);
						const inp = document.createElement("input");
						inp.type = "input";
						inp.value = arr[1] + " V";
						inp.addEventListener("change", event => {
							const v = parseFloat(event.currentTarget.value.replace("V").trim());
							const valid = !isNaN(v);
							const body = {
								channel : currentDevice.ADDRESS,
								paramset_key : "MASTER",
								key : "LOW_BAT_LIMIT",
								value : v + ""
							};
							const params2 = new URLSearchParams();
							params2.set("address", currentDevice.ADDRESS);
							params2.set("param", "MASTER");
							const p2 = !valid ? Promise.resolve(null) :
								send("POST", "setparam", undefined, undefined, body);
							p2.finally(() => send("GET", "paramread", undefined, params2).then(res => inp.value = res.LOW_BAT_LIMIT + " V"));
							
						});
//						newLabel(arr[1] + " V" + " (default: " + arr[2] + " V)", div);
						const cont = document.createElement("div");
						cont.appendChild(inp);
						newLabel(" (default: " + arr[2] + " V)", cont).classList.add("margin-left");;
						div.appendChild(cont);
					}
					else {
						newLabel(undefined, div);
						newLabel(undefined, div);
					}
					newLabel(undefined, div);
					frag.appendChild(div);
				}); 
			}).catch(e => console.log("error",e)));
		});
		Promise.all(promises).then(() => grid.appendChild(frag));
	};
	
	const readRoomClimateData = () => {
		const list = document.getElementById("roomClimateGrid");
		clear(list, ["SPAN"]);
		const frag = document.createDocumentFragment();
		const promises = [];
		const thSensors = getTHSensors();
		thSensors.forEach(th => {
			const params = new URLSearchParams();
			params.set("address", th.ADDRESS + ":1");
			params.set("key", "HUMIDITY"); // readvalue not working!
//			params.set("param", "MASTER");
			const currentSensor = th;
			promises.push(send("GET", "readvalue", undefined, params).then(respHum => {
				params.set("key","TEMPERATURE")
				return send("GET", "readvalue", undefined, params).then(respTemp => {
					const div = document.createElement("div");
					div.style.display = "contents";
					let text = currentSensor.ADDRESS + " (TH sensor";
					if (currentSensor.room)
						text = text + ", " + currentSensor.room; 
					text = text + ")";
					newLabel(text, div);
					newLabel(respTemp.result + "°C", div);
					newLabel(respHum.result + "%", div);
					newLabel(undefined, div);
					newLabel(undefined, div);
					frag.appendChild(div);
				});
			}));
		});
		const thermos = getThermostats();
		thermos.forEach(thermo => {
			const params = new URLSearchParams();
			params.set("address", thermo.ADDRESS + ":4");
			params.set("key", "ACTUAL_TEMPERATURE"); // readvalue not working!
//			params.set("param", "MASTER");
			const currentThermo = thermo;
			promises.push(send("GET", "readvalue", undefined, params).then(respTemp => {
				params.set("key", "VALVE_STATE");
				return send("GET", "readvalue", undefined, params).then(respValve => {
					const div = document.createElement("div");
					div.style.display = "contents";
					let text = currentThermo.ADDRESS + " (Thermostat";
					if (currentThermo.room)
						text = text + ", " + currentThermo.room;
					text += ")";
					newLabel(text, div);
					newLabel(respTemp.result + "°C", div);
					newLabel(undefined, div);
					newLabel(respValve.result + "%", div);
					newLabel(undefined, div);
					frag.appendChild(div);
				});
			}));
		});
		Promise.all(promises).then(() => list.appendChild(frag));
	};
	
	const getInstallModeState = () => {
		/*
		params.set("address", "BidCoS-RF:0");
		params.set("key", "INSTALL_MODE");
		*/
		return send("GET", "installmode").then(resp => resp.seconds > 0); // TODO display seconds!
	}

	const setInstallModeState = state => {
		/*
		const body = {
			channel: "BidCoS-RF:0",
			paramset_key: "VALUES",
			key: "INSTALL_MODE",
			value: state + ""
		};
		return send("POST", "setvalue", undefined, undefined, body).then(resp => resp.INSTALL_MODE);
		*/
		const body = {
				mode: state
			};
		return send("POST", "installmode", undefined, undefined, body).then(resp => resp.seconds > 0);
	};
	
	const updateInstallMode = () => {
		getInstallModeState().then(state => {
			document.getElementById("installMode").innerText = state;
			document.getElementById("installModeTrigger").value = state ? "Stop pairing" : "Start pairing";
		});
	};
	
	const initSpecialFunctions = () => {
		document.getElementById("specialFunctionSelect").addEventListener("change", event => {
			const selected = event.currentTarget.value;
			if (!devices) {
				setTimeout(() => document.getElementById("specialFunctionSelect").dispatchEvent(new Event("change")), 500);
				return;
			}
			switch (selected) {
			case "roomClimate":
				readRoomClimateData();
				break;
			case "thermostats":
				updateThermostats();
				break;
			case "power":
				updatePower();
				break;
			case "unreach":
				updateUnreach();
				break;
			case "battery":
				updateBattery();
				break;
			case "pairing":
				updateInstallMode();
				break;
			default:
				break;
			}
			Array.from(document.querySelectorAll(".special-function"))
				.forEach(div => div.id === selected + "Container" ? div.dataset.state = "visible" : div.removeAttribute("data-state"));
			const updateVisible = selected !== "empty";
			const updateBtn = document.getElementById("updateFunctionData");
			if (updateVisible)
				updateBtn.dataset.state = "visible";
			else
				updateBtn.removeAttribute("data-state");
			pushStateInternal();
		});
		 document.getElementById("updateFunctionData").addEventListener("click", () => document.getElementById("specialFunctionSelect").dispatchEvent(new Event("change")));
		 document.getElementById("powerAllOff").addEventListener("click", () => {
			 const promises= [];
			 getPowerSensors().forEach(p => {
				 const postParams = {
					channel : p.ADDRESS + ":1",
					paramset_key : "VALUES",
					key : "STATE",
					value : "false"
				 };
				 promises.push(send("POST", "setvalue", undefined, undefined, postParams));
			 });
			 Promise.all(promises).then(() => setTimeout(updatePower,1000));
		 });
		 document.getElementById("installModeTrigger").addEventListener("click", 
				 () => getInstallModeState().then(state => setInstallModeState(!state)).then(updateInstallMode));
		 document.getElementById("modeForAllSubmit").addEventListener("click", () => {
			 const value = document.getElementById("modeForAll").value;
			 showSpinner("Updating thermostat modes");
			 Promise.all(getThermostats().map(thermo => {
				 const body = {
					channel: thermo.ADDRESS + ":4",
					paramset_key: "VALUES",
					key: "CONTROL_MODE",
					value: value
				 };
				 return send("POST", "setvalue", undefined, undefined, body);
			 })).then(updateThermostats)
			 .finally(hideSpinner);
		 });
		 document.getElementById("setpointForAllSubmit").addEventListener("click", () => {
			 const value = parseFloat(document.getElementById("setpointForAll").value.replace("°","").replace("C","").trim());
			 if (isNaN(value))
				 return;
			 showSpinner("Updating setpoints");
			 Promise.all(getThermostats().map(thermo => {
				 const body = {
					channel: thermo.ADDRESS + ":4",
					paramset_key: "VALUES",
					key: "SET_TEMPERATURE",
					value: value + ""
				 };
				 return send("POST", "setvalue", undefined, undefined, body);
			 })).then(updateThermostats)
			 .finally(hideSpinner);
		 });
		 document.getElementById("lockForAllSubmit").addEventListener("click", () => {
			 const value = document.getElementById("lockForAll").checked + "";
			 showSpinner("Updating thermostat locks");
			 Promise.all(getThermostats().map(thermo => {
				 const body = {
					channel: thermo.ADDRESS,
					paramset_key: "MASTER",
					key: "BUTTON_LOCK",
					value: value
				 };
				 return send("POST", "setparam", undefined, undefined, body);
			 })).then(updateThermostats)
			 .finally(hideSpinner);
		 });
	};
	
	const appendInfoDialog = () => {
		const diag = createDialog("infoDialog");
		const alert = document.createElement("span");
		alert.classList.add("alert");
		diag.appendChild(alert);
		document.body.appendChild(diag);
		infoAlertPromise = registerDialog(diag);
	};
	
	const initParams = () => {
		const search = new URLSearchParams(window.location.search);
		if (search.has("function")) {
			const select = document.getElementById("specialFunctionSelect");
			select.value = search.get("function");
			select.dispatchEvent(new Event("change"));
		}
	};
	
	const initInterfaces = () => {
		const ifc = document.getElementById("ifcSelect");
		clear(ifc);
		ifc.addEventListener("change", () => {
			updateDevices();
			pushStateInternal();
		});
		return send("GET", "interfaces", "Loading interfaces...").then(resp => {
			const frag = document.createDocumentFragment();
			const format = hmIfc => {
				let text = "Homematic " + hmIfc.type;
				if ("url" in hmIfc)
					text = text + " (" + hmIfc.url + ")";
				return text;
			};
			resp.forEach(opt => createOption(opt.id, format(opt), frag));
			ifc.appendChild(frag);
			const search = new URLSearchParams(window.location.search);
			if (search.has("interface"))
				ifc.value = search.get("interface");
			ifc.dispatchEvent(new Event("change"));
		});
	};
	
	// init
	
	appendInfoDialog();
	appendParamsDialog();
	initInterfaces().then(() => {
//		updateDevices(); // now triggered directly by initInterfaces
		initSorting(); // must happen after appendParamsDialog
		setFilters();
		initSpecialFunctions();
		initParams();
		appendLinkDialog();
		appendRemoveLinkDialog();
		initialized = true;
		pushStateInternal();
	});
	
})()