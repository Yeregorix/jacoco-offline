/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.jacoco;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.project.IsolatedAntBuilder;
import org.gradle.testing.jacoco.tasks.rules.JacocoLimit;
import org.gradle.testing.jacoco.tasks.rules.JacocoViolationRule;
import org.gradle.testing.jacoco.tasks.rules.JacocoViolationRulesContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;

public class AntJacocoCheck {

    private static final Predicate<JacocoViolationRule> RULE_ENABLED_PREDICATE = new Predicate<JacocoViolationRule>() {
        @Override
        public boolean apply(JacocoViolationRule rule) {
            return rule.isEnabled();
        }
    };

    private final IsolatedAntBuilder ant;

    public AntJacocoCheck(IsolatedAntBuilder ant) {
        this.ant = ant;
    }

    public void execute(FileCollection classpath, final String projectName,
                        final FileCollection allClassesDirs, final FileCollection allSourcesDirs,
                        final FileCollection executionData,
                        final JacocoViolationRulesContainer violationRules) {
        ant.withClasspath(classpath).execute(new Closure<Object>(this, this) {
            @SuppressWarnings("UnusedDeclaration")
            public Object doCall(Object it) {
                final GroovyObjectSupport antBuilder = (GroovyObjectSupport) it;
                antBuilder.invokeMethod("taskdef", ImmutableMap.of(
                        "name", "jacocoReport",
                        "classname", "org.jacoco.ant.ReportTask"
                ));
                final Map<String, Object> emptyArgs = Collections.<String, Object>emptyMap();
                antBuilder.invokeMethod("jacocoReport", new Object[]{emptyArgs, new Closure<Object>(this, this) {
                    public Object doCall(Object ignore) {
                        antBuilder.invokeMethod("executiondata", new Object[]{emptyArgs, new Closure<Object>(this, this) {
                            public Object doCall(Object ignore) {
                                executionData.addToAntBuilder(antBuilder, "resources");
                                return null;
                            }
                        }});
                        Map<String, Object> structureArgs = ImmutableMap.<String, Object>of("name", projectName);
                        antBuilder.invokeMethod("structure", new Object[]{structureArgs, new Closure<Object>(this, this) {
                            public Object doCall(Object ignore) {
                                antBuilder.invokeMethod("classfiles", new Object[]{emptyArgs, new Closure<Object>(this, this) {
                                    public Object doCall(Object ignore) {
                                        allClassesDirs.addToAntBuilder(antBuilder, "resources");
                                        return null;
                                    }
                                }});
                                antBuilder.invokeMethod("sourcefiles", new Object[]{emptyArgs, new Closure<Object>(this, this) {
                                    public Object doCall(Object ignore) {
                                        allSourcesDirs.addToAntBuilder(antBuilder, "resources");
                                        return null;
                                    }
                                }});
                                return null;
                            }
                        }});
                        configureCheck(antBuilder, violationRules);
                        return null;
                    }
                }});
                return null;
            }
        });
    }

    private void configureCheck(final GroovyObjectSupport antBuilder, final JacocoViolationRulesContainer violationRules) {
        if (!violationRules.getRules().isEmpty()) {
            Map<String, Object> checkArgs = ImmutableMap.<String, Object>of("failonviolation", !violationRules.isFailOnViolation());
            antBuilder.invokeMethod("check", new Object[] {checkArgs, new Closure<Object>(this, this) {
                @SuppressWarnings("UnusedDeclaration")
                public Object doCall(Object ignore) {
                    for (final JacocoViolationRule rule : filter(violationRules.getRules(), RULE_ENABLED_PREDICATE)) {
                        Map<String, Object> ruleArgs = new HashMap<String, Object>();

                        if (rule.getElement() != null) {
                            ruleArgs.put("element", rule.getElement());
                        }
                        if (rule.getIncludes() != null && !rule.getIncludes().isEmpty()) {
                            ruleArgs.put("includes", Joiner.on(':').join(rule.getIncludes()));
                        }
                        if (rule.getExcludes() != null && !rule.getExcludes().isEmpty()) {
                            ruleArgs.put("excludes", Joiner.on(':').join(rule.getExcludes()));
                        }

                        antBuilder.invokeMethod("rule", new Object[] {ImmutableMap.copyOf(ruleArgs), new Closure<Object>(this, this) {
                            @SuppressWarnings("UnusedDeclaration")
                            public Object doCall(Object ignore) {
                                for (JacocoLimit limit : rule.getLimits()) {
                                    Map<String, Object> ruleArgs = new HashMap<String, Object>();

                                    if (limit.getCounter() != null) {
                                        ruleArgs.put("counter", limit.getCounter());
                                    }
                                    if (limit.getValue() != null) {
                                        ruleArgs.put("value", limit.getValue());
                                    }
                                    if (limit.getMinimum() != null) {
                                        ruleArgs.put("minimum", limit.getMinimum());
                                    }
                                    if (limit.getMaximum() != null) {
                                        ruleArgs.put("maximum", limit.getMaximum());
                                    }

                                    antBuilder.invokeMethod("limit", new Object[] {ImmutableMap.copyOf(ruleArgs) });
                                }
                                return null;
                            }
                        }});
                    }
                    return null;
                }
            }});
        }
    }
}
