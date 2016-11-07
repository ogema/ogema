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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.impl.administration;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdminLogger;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.administration.PatternCondition;
import org.ogema.core.administration.RegisteredAccessModeRequest;
import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.administration.RegisteredResourceDemand;
import org.ogema.core.administration.RegisteredResourceListener;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.administration.RegisteredTimer;
import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

/**
 * 
 * @author jlapp
 */
@Component(specVersion = "1.2", immediate = true)
@Properties( { @Property(name = "osgi.command.scope", value = "ogm"),
		@Property(name = "osgi.command.function", value = { "apps", "clock", "loggers", "log", "dump_cache" }) })
@Service(ShellCommands.class)
@Descriptor("OGEMA administration commands")
public class ShellCommands {

	@Reference
	protected AdministrationManager admin;

	@Descriptor("list running OGEMA apps")
	public void apps(
			@Descriptor("show listeners registered by app") @Parameter(names = { "-l", "--listeners" }, presentValue = "true", absentValue = "false") boolean listeners) {
		apps(listeners, null);
	}

	protected String getListenerName(Object listener) {
		try {
			if (!Object.class.equals(listener.getClass().getMethod("toString").getDeclaringClass())) {
				return listener.toString();
			}
		} catch (NoSuchMethodException ex) {
			// nevermind
		}
		String name = listener.getClass().getCanonicalName();
		if (name == null) {
			name = "<unnamed class>";
		}
		return name;
	}

	@Descriptor("list running OGEMA apps")
	public void apps(
			@Descriptor("show listeners registered by app") @Parameter(names = { "-l", "--listeners" }, presentValue = "true", absentValue = "false") boolean listeners,
			@Descriptor("substring matched against application or bundle name") String pattern) {
		for (AdminApplication app : admin.getAllApps()) {
			String appName = app.getID().getApplication().getClass().getCanonicalName();
			String bundleName = app.getBundleRef().getSymbolicName();
			if (pattern != null) {
				if (!(appName.toUpperCase().contains(pattern.toUpperCase()) || (bundleName.toUpperCase()
						.contains(pattern.toUpperCase())))) {
					continue;
				}
			}
			System.out.printf("%s (%s, %d)%n", appName, bundleName, app.getBundleRef().getBundleId());
			if (listeners) {
				if (!app.getTimers().isEmpty()) {
					System.out.printf("  timers:%n");
					for (RegisteredTimer t : app.getTimers()) {
						System.out.printf("    every %d ms:%n", t.getTimer().getTimingInterval());
						for (TimerListener tl : t.getListeners()) {
							System.out.printf("      %s%n", getListenerName(tl));
						}
					}
				}
				if (!app.getResourceDemands().isEmpty()) {
					System.out.printf("  type demands:%n");
					for (RegisteredResourceDemand rrd : app.getResourceDemands()) {
						System.out.printf("    %s: %s%n", rrd.getTypeDemanded(), getListenerName(rrd.getListener()));
					}
				}
				if (!app.getAccessModeRequests().isEmpty()) {
					System.out.printf("  access mode requests:%n");
					for (RegisteredAccessModeRequest ramr : app.getAccessModeRequests()) {
						System.out.printf("    %s: %s (%s): %b%n", ramr.getResource().getPath(), ramr
								.getRequiredAccessMode(), ramr.getPriority(), ramr.isFulfilled());
					}
				}
				if (!app.getResourceListeners().isEmpty() || !app.getValueListeners().isEmpty()) {
					System.out.printf("  change listeners:%n", app.getResourceListeners());
					for (RegisteredResourceListener rrl : app.getResourceListeners()) {
						System.out.printf("    %s: %s%n", rrl.getResource().getPath(), getListenerName(rrl
								.getListener()));
					}
					for (RegisteredValueListener rvl : app.getValueListeners()) {
						System.out.printf("    %s: %s (%s)%n", rvl.getResource().getPath(), getListenerName(rvl
								.getValueListener()), rvl.isCallOnEveryUpdate() ? "on update" : "on change");
					}
				}
				if (!app.getStructureListeners().isEmpty()) {
					System.out.printf("  structure listeners:%n");
					for (RegisteredStructureListener rsl : app.getStructureListeners()) {
						System.out.printf("    %s: %s%n", rsl.getResource().getPath(), getListenerName(rsl
								.getListener()));
					}
				}
                if (!app.getPatternListeners().isEmpty()) {
                    System.out.printf("  pattern listeners:%n");
                    for (RegisteredPatternListener rpl: app.getPatternListeners()) {
                        System.out.printf("    %s: %s%n", rpl.getPatternDemandedModelType(), getListenerName(rpl.getListener()));
                        if (!rpl.getCompletedPatterns().isEmpty()) {
                            System.out.printf("    complete:%n");
                            for (ResourcePattern<?> completedPattern: rpl.getCompletedPatterns()) {
                                System.out.printf("      %s%n", completedPattern.model.getPath());
                            }
                        }
                        if (!rpl.getIncompletePatterns().isEmpty()) {
                            System.out.printf("    incomplete:%n");
                            for (ResourcePattern<?> incompletePattern: rpl.getIncompletePatterns()) {
                                System.out.printf("      %s%n", incompletePattern.model.getPath());
                                for (PatternCondition cond: rpl.getConditions(incompletePattern)) {
                                    if (!cond.isSatisfied()) {
                                        StringBuilder state = new StringBuilder(cond.getPath()).append(" ");
                                        if (cond.exists()) {
                                            state.append("(exists) ");
                                        } else {
                                            if (!cond.isOptional()) {
                                                state.append("(missing) ");
                                            }
                                        }
                                        if (!cond.isActive()) {
                                            state.append("(inactive) ");
                                        }
                                        System.out.printf("        %s: %s%n", cond.getFieldName(), state);
                                    }
                                }
                            }
                        }
                    }
                }
			}
		}
	}

	@Descriptor("Display framework clock settings")
	public void clock(
			@Descriptor("set the simulation factor (value>=0)") @Parameter(names = { "-f", "--factor" }, absentValue = "-1.0") float factor) {
		FrameworkClock cl = admin.getFrameworkClock();
		if (factor >= 0) {
			cl.setSimulationFactor(factor);
		}
		System.out.printf("%s%n%tc\tfactor=%f%n", cl.getName(), cl.getExecutionTime(), cl.getSimulationFactor());
	}

	@Descriptor("List loggers")
	public void loggers() {
		loggers("", "", "");
	}

	@Descriptor("List/configure loggers")
	public void loggers(
			@Descriptor("set log level for selected loggers") @Parameter(names = { "-l", "--level" }, absentValue = "") String level,
			@Descriptor("comma separated list of outputs (file, console or cache) for which to set the log level (default: all outputs)") @Parameter(names = {
					"-o", "--output" }, absentValue = "") String output,
			@Descriptor("select loggers by regex (case-insensitive subsequence match)") String match) {
		List<AdminLogger> loggers = admin.getAllLoggers();
		Collections.sort(loggers, new Comparator<AdminLogger>() {

			@Override
			public int compare(AdminLogger o1, AdminLogger o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
			}

		});
		Pattern p = null;
		List<LogOutput> outputs = Arrays.asList(LogOutput.values());
		if (!match.isEmpty()) {
			p = Pattern.compile(match, Pattern.CASE_INSENSITIVE);
		}
		if (!output.isEmpty()) {
			outputs = new ArrayList<>(4);
			for (String o : output.split(",")) {
				outputs.add(LogOutput.valueOf(o.toUpperCase()));
			}
		}
		for (AdminLogger l : loggers) {
			String loggerName = l.getName();
			if (p != null && !p.matcher(loggerName).find()) {
				continue;
			}
			if (!level.isEmpty()) {
				LogLevel ll = LogLevel.valueOf(level.toUpperCase());
				for (LogOutput o : outputs) {
					l.setMaximumLogLevel(o, ll);
				}
			}
			System.out.printf("  %s {%s=%s, %s=%s, %s=%s}%n", loggerName, LogOutput.CONSOLE,
					l.getMaximumLogLevel(LogOutput.CONSOLE), LogOutput.FILE, l.getMaximumLogLevel(LogOutput.FILE),
					LogOutput.CACHE, l.getMaximumLogLevel(LogOutput.CACHE));
		}
	}

	@Descriptor("Print recent log entries")
	public void log (
			@Descriptor("set log message limit (use negative value to start from end of cache (most recent message))") @Parameter(names = {
					"-l", "--limit" }, absentValue = "0") int limit,
            @Descriptor("print log messages to file instead of console, does not work with -l")
            @Parameter(names = {"-f", "--file" }, absentValue = "") String filename,
			@Descriptor("regex for filtering log messages (case insensitive substring match)") String pattern)  throws IOException {
		Pattern p = pattern == null || pattern.isEmpty() ? null : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        if (!filename.isEmpty()){
            try (PrintStream out = new PrintStream(filename, "UTF-8")){
                printCache(out, p, limit);
                System.out.printf("log written to %s%n", filename);
            }
        } else {
            printCache(System.out, p, limit);
        }
	}

	@Descriptor("Print recent log entries")
	public void log(
			@Descriptor("set log message limit (use negative value to start from end of cache (most recent message))") @Parameter(names = {
					"-l", "--limit" }, absentValue = "0") int limit,
			@Descriptor("print log messages to file instead of console, does not work with -l") @Parameter(names = {
					"-f", "--file" }, absentValue = "") String filename) throws IOException {
		log(0, "", "");
	}

	public void printCache(PrintStream out, Pattern filter, int limit) {
		List<String> cache = admin.getAllLoggers().get(0).getCache();
		List<String> results = new ArrayList<>();
		int todo = limit == 0 ? Integer.MAX_VALUE : Math.abs(limit);
		for (int i = 0; i < cache.size() && todo > 0; i++) {
			int idx = limit < 0 ? cache.size() - 1 - i : i;
			String s = cache.get(idx);
			if (filter != null && !filter.matcher(s).find()) {
				continue;
			}
			results.add(s);
			todo--;
		}
		if (limit < 0) {
			Collections.reverse(results);
		}
		for (String s : results) {
			out.print(s);
		}
	}

	@Descriptor("writes the current logger cache to disk")
	public void dump_cache() {
		boolean success = admin.getAllLoggers().get(0).saveCache();
		System.out.println(success ? "ok" : "failed");
	}

}
