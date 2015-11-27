package org.ogema.driver.zwave.manager;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.ValueId;

/**
 * 
 * @author baerthbn
 * 
 */
public class InputHandler implements NotificationWatcher {
	LocalDevice localDevice;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");

	public InputHandler(LocalDevice localDevice) {
		this.localDevice = localDevice;
	}

	@Override
	public void onNotification(Notification notification, Object context) {
		try {
			switch (notification.getType()) {

			// DEVICE STATUS
			// Kommt direkt als erstes beim Starten oder beim resetten
			case DRIVER_READY:
				logger.info(String.format("Driver ready\n" + "\thome id: %d", notification.getHomeId()));
				localDevice.setHomeId(notification.getHomeId());
				break;
			case DRIVER_FAILED:
				logger.info("Driver failed");
				break;
			case DRIVER_RESET:
				logger.info("Driver reset");
				break;

			// ALL_NODES STATUS
			// Meldungen beim Start nachdem alle Nodes geadded worden sind, sagt quasi ob was fehlt
			case AWAKE_NODES_QUERIED:
				logger.info("Awake nodes queried");
				break;
			case ALL_NODES_QUERIED:
				logger.info("All nodes queried");
				localDevice.setReady(true);
				break;
			case ALL_NODES_QUERIED_SOME_DEAD:
				logger.info("All nodes queried some dead");
				break;
			// POLLING
			case POLLING_ENABLED:
				logger.info("Polling enabled");
				break;
			case POLLING_DISABLED:
				logger.info("Polling disabled");
				break;

			// NODE STATUS
			case NODE_NEW:
				// kommt immer wenn eine node zwar schon im netzwerk ist aber nicht in der configdatei
				logger.info(String.format("Node new\n" + "\tnode id: %d", notification.getNodeId()));
				break;
			case NODE_ADDED:
				// kommt immer wenn eine neue node hinzugefügt wird
				logger.info(String.format("Node added\n" + "\tnode id: %d", notification.getNodeId()));
				Node tempNode;
				long home = notification.getHomeId();
				short nodeId = notification.getNodeId();
				if (nodeId == 0)
					break;// skip invalid, incomplete node
				String name = "unknown";
				if (nodeId == 1)
					name = "Network-Coordinator";
				else if (localDevice.isWaitingNodeName())
					name = localDevice.getWaitingNodeName();
				else
					name = Manager.get().getNodeName(home, nodeId);

				if (name == null || name.equals("")) {
					Manager.get().removeNode(home);
				}
				else {
					tempNode = new Node(localDevice, nodeId, name);
					localDevice.addNode(tempNode);
				}
				break;
			case NODE_REMOVED:
				// kommt immer wenn eine vorhandene node entfernt wird
				nodeId = notification.getNodeId();
				logger.info(String.format("Node removed\n" + "\tnode id: %d", nodeId));
				// String nodeName = Manager.get().getNodeName(notification.getHomeId(), notification.getNodeId());
				localDevice.removeNode(nodeId);
				// TODO: causes problems because there is nothing implemented for removing devices and its channels yet
				break;

			case ESSENTIAL_NODE_QUERIES_COMPLETE:
				// nur die coordinator node geadded
				System.out.println(String.format("Node essential queries complete\n" + "\tnode id: %d", notification
						.getNodeId()));
				localDevice.setReadyId();
				break;
			case NODE_QUERIES_COMPLETE:
				// sagt er immer wenn eine node hinzugefügt wurde
				System.out
						.println(String.format("Node queries complete\n" + "\tnode id: %d", notification.getNodeId()));
				Iterator<Entry<Short, Node>> it = localDevice.getNodes().entrySet().iterator();
				while (it.hasNext()) {
					Entry<Short, Node> pair = it.next();
					pair.getValue().setReady(true);
				}
				break;
			case NODE_EVENT:
				// wenn event eintritt
				System.out.println(String.format("Node event\n" + "\tnode id: %d\n" + "\tevent id: %d", notification
						.getNodeId(), notification.getEvent()));
				break;
			case NODE_NAMING:
				// unbekannt
				System.out.println(String.format("Node naming\n" + "\tnode id: %d", notification.getNodeId()));
				break;
			case NODE_PROTOCOL_INFO:
				// gibt den type der node an
				System.out.println(String.format("Node protocol info\n" + "\tnode id: %d\n" + "\ttype: %s",
						notification.getNodeId(), localDevice.getManager().getNodeType(notification.getHomeId(),
								notification.getNodeId())));
				break;

			// VALUE STATUS
			case VALUE_ADDED:
				// value zu nodes hinzufügen
				nodeId = notification.getNodeId();
				ValueId valueId = notification.getValueId();
				logger.debug(String.format("Value added\n" + "\tnode id: %d\n" + "\tcommand class: %d\n"
						+ "\tinstance: %d\n" + "\tindex: %d\n" + "\tgenre: %s\n" + "\ttype: %s\n" + "\tlabel: %s\n"
						+ "\tvalue: %s", nodeId, notification.getValueId().getCommandClassId(), valueId.getInstance(),
						valueId.getIndex(), valueId.getGenre().name(), valueId.getType().name(), localDevice
								.getManager().getValueLabel(valueId), getValue(valueId)));
				// neu
				Node n = localDevice.getNodes().get(nodeId);
				NodeValue nv = new NodeValue(localDevice.getManager(), notification.getValueId(), n
						.generateChannelAddress(notification.getValueId()));
				// n.getValues().put(nv.getChannelAddress(), nv);
				n.addValue(nv);
				// nv.valueChanged();
				break;
			case VALUE_REMOVED:
				// value von node entfernen
				System.out.println(String.format("Value removed\n" + "\tnode id: %d\n" + "\tcommand class: %d\n"
						+ "\tinstance: %d\n" + "\tindex: %d", notification.getNodeId(), notification.getValueId()
						.getCommandClassId(), notification.getValueId().getInstance(), notification.getValueId()
						.getIndex()));
				// neu
				// Node nn = localDevice.getNodes().get(Manager.get().getNodeName(notification.getHomeId(),
				// notification.getNodeId()));
				// nn.getValues().remove(nn.generateChannelAddress(notification.getValueId()));
				// TODO: causes problems because there is nothing for deleting channels yet
				break;
			case VALUE_CHANGED:
				// wert verändert
				nodeId = notification.getNodeId();
				valueId = notification.getValueId();
				System.out.println(String.format("Value changed\n" + "\tnode id: %d\n" + "\tcommand class: %d\n"
						+ "\tinstance: %d\n" + "\tindex: %d\n" + "\tvalue: %s", nodeId, valueId.getCommandClassId(),
						valueId.getInstance(), valueId.getIndex(), getValue(valueId)));
				// neu
				Node nnn = localDevice.getNodes().get(nodeId);
				nnn.getValues().get(nnn.generateChannelAddress(notification.getValueId())).valueChanged();
				break;
			case VALUE_REFRESHED:
				// wert erneuert ??
				nodeId = notification.getNodeId();
				valueId = notification.getValueId();
				System.out.println(String.format("Value refreshed\n" + "\tnode id: %d\n" + "\tcommand class: %d\n"
						+ "\tinstance: %d\n" + "\tindex: %d" + "\tvalue: %s", nodeId, valueId.getCommandClassId(),
						valueId.getInstance(), valueId.getIndex(), getValue(valueId)));
				// neu
				Node nnnn = localDevice.getNodes().get(nodeId);
				nnnn.getValues().get(nnnn.generateChannelAddress(notification.getValueId())).valueChanged();
				break;

			// GROUP & SCENES ARE NYI
			case GROUP:
				System.out.println(String.format("Group\n" + "\tnode id: %d\n" + "\tgroup id: %d", notification
						.getNodeId(), notification.getGroupIdx()));
				break;
			case SCENE_EVENT:
				System.out.println(String.format("Scene event\n" + "\tscene id: %d", notification.getSceneId()));
				break;
			case CREATE_BUTTON:
				System.out.println(String.format("Button create\n" + "\tbutton id: %d", notification.getButtonId()));
				break;
			case DELETE_BUTTON:
				System.out.println(String.format("Button delete\n" + "\tbutton id: %d", notification.getButtonId()));
				break;
			case BUTTON_ON:
				System.out.println(String.format("Button on\n" + "\tbutton id: %d", notification.getButtonId()));
				break;
			case BUTTON_OFF:
				System.out.println(String.format("Button off\n" + "\tbutton id: %d", notification.getButtonId()));
				break;

			// UNIMPLEMENTED YET
			case NOTIFICATION:
				logger.info(notification.getNotification().toString());
				break;
			case CONTROLLER_COMMAND:
				logger.info("Controller_Command");
				break;
			case DRIVER_REMOVED:
				logger.info("DRIVER_REMOVED");
				break;

			default:
				System.out.println(notification.getType().name());
				break;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private Object getValue(ValueId valueId) {
		switch (valueId.getType()) {
		case BOOL:
			AtomicReference<Boolean> b = new AtomicReference<>();
			Manager.get().getValueAsBool(valueId, b);
			return b.get();
		case BYTE:
			AtomicReference<Byte> bb = new AtomicReference<>();
			Manager.get().getValueAsByte(valueId, bb);
			return bb.get();
		case DECIMAL:
			AtomicReference<Float> f = new AtomicReference<>();
			Manager.get().getValueAsFloat(valueId, f);
			return f.get();
		case INT:
			AtomicReference<Integer> i = new AtomicReference<>();
			Manager.get().getValueAsInt(valueId, i);
			return i.get();
		case LIST:
			return null;
		case SCHEDULE:
			return null;
		case SHORT:
			AtomicReference<Short> s = new AtomicReference<>();
			Manager.get().getValueAsShort(valueId, s);
			return s.get();
		case STRING:
			AtomicReference<String> ss = new AtomicReference<>();
			Manager.get().getValueAsString(valueId, ss);
			return ss.get();
		case BUTTON:
			return null;
		case RAW:
			AtomicReference<byte[]> sss = new AtomicReference<>();
			Manager.get().getValueAsRaw(valueId, sss);
			return sss.get();
		default:
			return null;
		}
	}
}
