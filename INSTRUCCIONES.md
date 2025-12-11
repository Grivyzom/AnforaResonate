# Indicaciones que ha solicitado el cliente

## A continuación se te deja un listado de lo que me ha solicitado mi cliente:

Salio este error.

17:03:09 WARN]: [AnforaResonate] Plugin AnforaResonate v1.0-SNAPSHOT generated an exception while executing task 10099
java.lang.NullPointerException: Nodes must be provided.
        at org.yaml.snakeyaml.nodes.NodeTuple.<init>(NodeTuple.java:26) ~[snakeyaml-2.2.jar:?]
        at org.yaml.snakeyaml.representer.BaseRepresenter.representMapping(BaseRepresenter.java:181) ~[snakeyaml-2.2.jar:?]
        at org.yaml.snakeyaml.representer.SafeRepresenter$RepresentMap.representData(SafeRepresenter.java:331) ~[snakeyaml-2.2.jar:?]
        at org.bukkit.configuration.file.YamlRepresenter$RepresentConfigurationSerializable.representData(YamlRepresenter.java:52) ~[paper-api-1.21.8-R0.1-SNAPSHOT.jar:?]
        at org.yaml.snakeyaml.representer.BaseRepresenter.representData(BaseRepresenter.java:111) ~[snakeyaml-2.2.jar:?]
        at org.yaml.snakeyaml.representer.BaseRepresenter.represent(BaseRepresenter.java:81) ~[snakeyaml-2.2.jar:?]
        at org.bukkit.configuration.file.YamlConfiguration.toNodeTree(YamlConfiguration.java:196) ~[paper-api-1.21.8-R0.1-SNAPSHOT.jar:?]
        at org.bukkit.configuration.file.YamlConfiguration.toNodeTree(YamlConfiguration.java:194) ~[paper-api-1.21.8-R0.1-SNAPSHOT.jar:?]
        at org.bukkit.configuration.file.YamlConfiguration.saveToString(YamlConfiguration.java:80) ~[paper-api-1.21.8-R0.1-SNAPSHOT.jar:?]
        at org.bukkit.configuration.file.FileConfiguration.save(FileConfiguration.java:65) ~[paper-api-1.21.8-R0.1-SNAPSHOT.jar:?]
        at AnforaXP.jar/gc.grivyzom.AnforaXP.data.YamlStorage.saveAnfora(YamlStorage.java:105) ~[AnforaXP.jar:?]
        at AnforaXP.jar/gc.grivyzom.AnforaXP.data.AnforaDataManager.saveAnfora(AnforaDataManager.java:26) ~[AnforaXP.jar:?]
        at AnforaXP.jar/gc.grivyzom.AnforaXP.listeners.GuiListener.lambda$withdrawExperience$2(GuiListener.java:253) ~[AnforaXP.jar:?]
        at org.bukkit.craftbukkit.scheduler.CraftTask.run(CraftTask.java:78) ~[paper-1.21.8.jar:1.21.8-27-a664311]
        at org.bukkit.craftbukkit.scheduler.CraftAsyncTask.run(CraftAsyncTask.java:57) ~[paper-1.21.8.jar:1.21.8-27-a664311]
        at com.destroystokyo.paper.ServerSchedulerReportingWrapper.run(ServerSchedulerReportingWrapper.java:22) ~[paper-1.21.8.jar:?]
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144) ~[?:?]
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642) ~[?:?]
        at java.base/java.lang.Thread.run(Thread.java:1583) ~[?:?]