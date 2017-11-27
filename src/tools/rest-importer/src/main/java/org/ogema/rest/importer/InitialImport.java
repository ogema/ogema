/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.rest.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.tools.SerializationManager;

public class InitialImport {

	private final OgemaLogger logger;
	private final SerializationManager sm;

	public InitialImport(ApplicationManager am) {
		this.logger = am.getLogger();
		this.sm = am.getSerializationManager();
	}

	public void startImport() {
		File folder = null;
		try {
			String indir = System.getProperty(Importer.IN_DIR_PROP);
			if (indir == null || indir.isEmpty() || indir.contains("..") || indir.startsWith("/")
					|| indir.startsWith("\\")) {
				indir = Importer.DEFAULT_DIR;
			}
			folder = new File(indir);
			importFiles(folder);
		} catch (Exception e) {
			logger.error("Could not read files from directory {}: {}", folder, e);
		}
	}

	private void importFiles(File dir) {
		File[] files = dir.listFiles();
		if (files == null || files.length==0) {
			return;
		}
		List<File> filesList = Arrays.asList(files);
		Collections.sort(filesList);
		logger.info("Trying to import {} files",filesList.size());
		for (File file : filesList) {
			if (file.isDirectory()) {
				importFiles(file);
				return;
			}
			BufferedReader br = null;
			logger.debug("Importing file {}",file.getPath());
			boolean useJson; 
			String pth = file.getPath().toLowerCase();
			if (pth.endsWith(".xml") || pth.endsWith(".ogx")) {  
				useJson = false;
			} 
			else if (pth.endsWith(".json") || pth.endsWith(".ogj")) {
				useJson = true;
			} 
			else {
				logger.debug("Invalid file name " + file.getName());
				continue;
			}
			StringBuilder sb = new StringBuilder();
			try {
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
				while (br.ready()) {
					sb.append(br.readLine() + "\n");
				}
				doPost(sb.toString(),useJson);
			    Thread.sleep(200);  // allow some time for callbacks to be executed, etc; do not remove
			} catch (InterruptedException | IOException e) {
				logger.warn("Could not import file {}: {}", file.getPath(), e);
			} finally {
				try {
					if (br != null)	br.close();
				} catch (IOException ee) {}
			}
		}	
	}

	private void doPost(String content, boolean useJson) {
		try {
			Resource resource = useJson ? sm.createFromJson(content) : sm.createFromXml(content);
			logger.debug("Resource created: {}", resource);
		} catch (Exception e) {
			logger.error("Could not create resource from {}",content, e);
		}
	}

}
