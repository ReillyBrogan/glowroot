/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glowroot.plugin.logger;

import java.util.Locale;

import org.glowroot.api.ErrorMessage;
import org.glowroot.api.MessageSupplier;
import org.glowroot.api.MetricName;
import org.glowroot.api.PluginServices;
import org.glowroot.api.Span;
import org.glowroot.api.weaving.BindMethodArg;
import org.glowroot.api.weaving.BindMethodName;
import org.glowroot.api.weaving.BindTraveler;
import org.glowroot.api.weaving.IsEnabled;
import org.glowroot.api.weaving.OnAfter;
import org.glowroot.api.weaving.OnBefore;
import org.glowroot.api.weaving.Pointcut;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
public class Log4jAspect {

    private static final PluginServices pluginServices = PluginServices.get("logger");

    @Pointcut(typeName = "org.apache.log4j.Category", methodName = "warn|error|fatal",
            methodArgs = {"java.lang.Object"}, metricName = "logging")
    public static class LogAdvice {
        private static final MetricName metricName =
                pluginServices.getMetricName(LogAdvice.class);
        @IsEnabled
        public static boolean isEnabled() {
            return pluginServices.isEnabled();
        }
        @OnBefore
        public static Span onBefore(@BindMethodArg Object message,
                @BindMethodName String methodName) {
            if (!methodName.equals("warn")) {
                pluginServices.setTraceError(String.valueOf(message));
            }
            return pluginServices.startSpan(
                    MessageSupplier.from("log {}: {}", methodName, String.valueOf(message)),
                    metricName);
        }
        @OnAfter
        public static void onAfter(@BindTraveler Span span) {
            span.end();
        }
    }

    @Pointcut(typeName = "org.apache.log4j.Category", methodName = "warn|error|fatal",
            methodArgs = {"java.lang.Object", "java.lang.Throwable"}, metricName = "logging")
    public static class LogWithThrowableAdvice {
        private static final MetricName metricName =
                pluginServices.getMetricName(LogWithThrowableAdvice.class);
        @IsEnabled
        public static boolean isEnabled() {
            return pluginServices.isEnabled();
        }
        @OnBefore
        public static Span onBefore(@BindMethodArg Object message,
                @BindMethodName String methodName) {
            if (!methodName.equals("warn")) {
                pluginServices.setTraceError(String.valueOf(message));
            }
            return pluginServices.startSpan(
                    MessageSupplier.from("log {}: {}", methodName, String.valueOf(message)),
                    metricName);
        }
        @OnAfter
        public static void onAfter(@SuppressWarnings("unused") @BindMethodArg Object message,
                @BindMethodArg Throwable t, @BindTraveler Span span) {
            if (t == null) {
                span.end();
            } else {
                span.endWithError(ErrorMessage.from(t));
            }
        }
    }

    @Pointcut(typeName = "org.apache.log4j.Category", methodName = "log",
            methodArgs = {"org.apache.log4j.Priority", "java.lang.Object"}, metricName = "logging")
    public static class LogWithPriorityAdvice {
        private static final MetricName metricName =
                pluginServices.getMetricName(LogWithPriorityAdvice.class);
        @IsEnabled
        public static boolean isEnabled(@BindMethodArg Object priority) {
            if (!pluginServices.isEnabled()) {
                return false;
            }
            String level = priority.toString();
            return level.equals("FATAL") || level.equals("ERROR") || level.equals("WARN");
        }
        @OnBefore
        public static Span onBefore(@BindMethodArg Object priority, @BindMethodArg Object message) {
            String level = priority.toString().toLowerCase(Locale.ENGLISH);
            if (!level.equals("warn")) {
                pluginServices.setTraceError(String.valueOf(message));
            }
            return pluginServices.startSpan(
                    MessageSupplier.from("log {}: {}", level, String.valueOf(message)), metricName);
        }
        @OnAfter
        public static void onAfter(@BindTraveler Span span) {
            span.end();
        }
    }

    @Pointcut(typeName = "org.apache.log4j.Category", methodName = "log",
            methodArgs = {"org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable"},
            metricName = "logging")
    public static class LogWithPriorityAndThrowableAdvice {
        private static final MetricName metricName =
                pluginServices.getMetricName(LogWithPriorityAndThrowableAdvice.class);
        @IsEnabled
        public static boolean isEnabled(@BindMethodArg Object priority) {
            if (!pluginServices.isEnabled()) {
                return false;
            }
            String level = priority.toString();
            return level.equals("FATAL") || level.equals("ERROR") || level.equals("WARN");
        }
        @OnBefore
        public static Span onBefore(@BindMethodArg Object priority, @BindMethodArg Object message) {
            String level = priority.toString().toLowerCase(Locale.ENGLISH);
            if (!level.equals("warn")) {
                pluginServices.setTraceError(String.valueOf(message));
            }
            return pluginServices.startSpan(
                    MessageSupplier.from("log {}: {}", level, String.valueOf(message)), metricName);
        }
        @OnAfter
        public static void onAfter(@SuppressWarnings("unused") @BindMethodArg Object priority,
                @SuppressWarnings("unused") @BindMethodArg Object message,
                @BindMethodArg Throwable t, @BindTraveler Span span) {
            if (t == null) {
                span.end();
            } else {
                span.endWithError(ErrorMessage.from(t));
            }
        }
    }

    @Pointcut(typeName = "org.apache.log4j.Category", methodName = "l7dlog",
            methodArgs = {"org.apache.log4j.Priority", "java.lang.String", "java.lang.Throwable"},
            metricName = "logging")
    public static class LocalizedLogAdvice {
        private static final MetricName metricName =
                pluginServices.getMetricName(LocalizedLogAdvice.class);
        @IsEnabled
        public static boolean isEnabled(@BindMethodArg Object priority) {
            if (!pluginServices.isEnabled()) {
                return false;
            }
            String level = priority.toString();
            return level.equals("FATAL") || level.equals("ERROR") || level.equals("WARN");
        }
        @OnBefore
        public static Span onBefore(@BindMethodArg Object priority, @BindMethodArg String key) {
            String level = priority.toString().toLowerCase(Locale.ENGLISH);
            if (!level.equals("warn")) {
                pluginServices.setTraceError(key);
            }
            return pluginServices.startSpan(
                    MessageSupplier.from("log {} (localized): {}", level, key), metricName);
        }
        @OnAfter
        public static void onAfter(@SuppressWarnings("unused") @BindMethodArg Object priority,
                @SuppressWarnings("unused") @BindMethodArg String key,
                @BindMethodArg Throwable t, @BindTraveler Span span) {
            if (t == null) {
                span.end();
            } else {
                span.endWithError(ErrorMessage.from(t));
            }
        }
    }

    @Pointcut(typeName = "org.apache.log4j.Category", methodName = "l7dlog",
            methodArgs = {"org.apache.log4j.Priority", "java.lang.String", "java.lang.Object[]",
                    "java.lang.Throwable"}, metricName = "logging")
    public static class LocalizedLogWithParametersAdvice {
        private static final MetricName metricName =
                pluginServices.getMetricName(LocalizedLogWithParametersAdvice.class);
        @IsEnabled
        public static boolean isEnabled(@BindMethodArg Object priority) {
            if (!pluginServices.isEnabled()) {
                return false;
            }
            String level = priority.toString();
            return level.equals("FATAL") || level.equals("ERROR") || level.equals("WARN");
        }
        @OnBefore
        public static Span onBefore(@BindMethodArg Object priority, @BindMethodArg String key,
                @BindMethodArg Object[] params) {
            String level = priority.toString().toLowerCase(Locale.ENGLISH);
            if (!level.equals("warn")) {
                pluginServices.setTraceError(key);
            }
            if (params.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(params[i]);
                }
                return pluginServices.startSpan(
                        MessageSupplier.from("log {} (localized): {} [{}]", level, key,
                                sb.toString()), metricName);
            } else {
                return pluginServices.startSpan(
                        MessageSupplier.from("log {} (localized): {}", level, key), metricName);
            }
        }
        @OnAfter
        public static void onAfter(@SuppressWarnings("unused") @BindMethodArg Object priority,
                @SuppressWarnings("unused") @BindMethodArg String key,
                @SuppressWarnings("unused") @BindMethodArg Object[] params,
                @BindMethodArg Throwable t, @BindTraveler Span span) {
            if (t == null) {
                span.end();
            } else {
                span.endWithError(ErrorMessage.from(t));
            }
        }
    }
}
