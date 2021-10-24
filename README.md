# SCOUTER-AGENT-CUBRID
For Monitoring CUBRID DBMS (https://github.com/CUBRID/cubrid)

 * Important
   - CUBRID 10.2.1 Version or later is required to use all features.
   - CUBRID Agent is supported since scouter version 2.15.0.
     (https://github.com/scouter-project/scouter/releases/tag/v2.15.0)

## HOW TO BUILD SCOUTER-AGENT-CUBRID

### Requirements

- JDK 1.8 (add %JAVA_HOME% environment variable)
- MAVEN (add %MAVEN_HOME% environment variable)

#### LINUX
```
build.sh
```
#### WINDOWS
```
build.bat
```

#### ECLIPSE
- import maven project using the pom.xml.
- need scouter-common.jar (Created by scouter-common-ver.2.10.0)

##### Output Files Info
- Package File : SourcePath/target/package/scouter-agent-cubrid-[Version].tar.gz
- Excute Files : SourcePath/target/scouter-cubrid-agent

## PORT INFO (DEFAULT)

- CUBRID Agent UDP : 6200 
- CUBRID Server HTTP : 8001
- SCOUTER Server(Collector) UDP : 6100
- SCOUTER Sserver(Collector) TCP : 6100

### Can be set in conf/scouter_cubrid.conf
```
net_local_udp_ip=127.0.0.1  # Agent IP
net_local_udp_port=6200     # Agent UDP
net_collector_ip=127.0.0.1  # SCOUTER Server IP
net_collector_udp_port=6100 # SCOUTER Server UDP Port
net_collector_tcp_port=6100 # SCOUTER Server UDP Port
cubrid_cms_ip=127.0.0.1     # Cubrid Manager Server IP
cubrid_cms_port=8001        # Cubrid Manager Server Port
```
## User Guild
- [Quick Start](documents/quick_start.md)
- [Client UI Guide](documents/client_guide.md)

