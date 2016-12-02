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

package org.gradle.internal.jacoco.rules;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.internal.ClosureBackedAction;
import org.gradle.testing.jacoco.tasks.rules.JacocoValidationRule;
import org.gradle.testing.jacoco.tasks.rules.JacocoValidationRulesContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JacocoValidationRulesContainerImpl implements JacocoValidationRulesContainer {

    private boolean ignoreFailures;
    private final List<JacocoValidationRule> rules = new ArrayList<JacocoValidationRule>();

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    @Override
    public boolean isIgnoreFailures() {
        return ignoreFailures;
    }

    @Override
    public List<JacocoValidationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public JacocoValidationRule rule(Closure configureClosure) {
        return rule(ClosureBackedAction.of(configureClosure));
    }

    @Override
    public JacocoValidationRule rule(Action<? super JacocoValidationRule> configureAction) {
        JacocoValidationRule validationRule = new JacocoValidationRuleImpl();
        configureAction.execute(validationRule);
        rules.add(validationRule);
        return validationRule;
    }
}
