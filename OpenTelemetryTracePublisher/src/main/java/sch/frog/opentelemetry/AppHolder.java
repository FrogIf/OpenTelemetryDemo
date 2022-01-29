package sch.frog.opentelemetry;

import sch.frog.opentelemetry.app.Application;
import sch.frog.opentelemetry.app.ProcessInfo;
import sch.frog.opentelemetry.app.SystemInfo;
import sch.frog.opentelemetry.app.TelemetryInfo;

public class AppHolder {

    public static final Application frogAppA = Application.Builder.newBuilder()
            .setServiceName("FrogAppA")
            .setSystemInfo(SystemInfo.Builder.newBuilder()
                    .setHostArch("amd64")
                    .setHostName("AAA")
                    .setOsType("win")
                    .setOsDescription("wwwwwww")
                    .build())
            .setProcessInfo(ProcessInfo.Builder.newBuilder()
                    .setCommandLine("java -jar aaa.jar")
                    .setPid(1239)
                    .setRuntimeVersion("jdk11")
                    .setExecutablePath("c:\\Program files\\jdk190\\java.exe")
                    .setRuntimeDescription("aaaaaaaaa")
                    .setRuntimeName("Open JDK")
                    .build())
            .setTelemetryInfo(TelemetryInfo.Builder.newBuilder()
                    .setAutoVersion("1.10.1")
                    .setSdkLanguage("java")
                    .setSdkName("opentelemetry")
                    .setSdkVersion("1.10.1")
                    .build())
            .addAttribute("frog-mark", "fake")
            .build();

    public static final Application frogAppB = Application.Builder.newBuilder()
            .setServiceName("FrogAppB")
            .setSystemInfo(SystemInfo.Builder.newBuilder()
                    .setHostArch("amd64")
                    .setHostName("AAA")
                    .setOsType("win")
                    .setOsDescription("wwwwwww")
                    .build())
            .setProcessInfo(ProcessInfo.Builder.newBuilder()
                    .setCommandLine("java -jar aaa.jar")
                    .setPid(7861)
                    .setRuntimeVersion("jdk11")
                    .setExecutablePath("c:\\Program files\\jdk190\\java.exe")
                    .setRuntimeDescription("aaaaaaaaa")
                    .setRuntimeName("Open JDK")
                    .build())
            .setTelemetryInfo(TelemetryInfo.Builder.newBuilder()
                    .setAutoVersion("1.10.1")
                    .setSdkLanguage("java")
                    .setSdkName("opentelemetry")
                    .setSdkVersion("1.10.1")
                    .build())
            .addAttribute("frog-mark", "fake")
            .build();

}
