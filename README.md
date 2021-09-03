# SCOUTER-AGENT-CUBRID
For Monitoring CUBRID DBMS (https://github.com/CUBRID/cubrid)

CUBRID Agent will working after scouter are updated.
## HOW TO BUILD/INSTALL SCOUTER-AGENT-CUBRID
### ECLIPSE
- import maven project using the pom.xml.
- need scouter-common.jar (Created by scouter-common-ver.2.10.0)
### LINUX
```
build.sh (MAVEN Project)
```
### WINDOWS
```
build.bat (MAVEN Project)
```

- Package Path : SourcePath/target/package/scouter-agent-cubrid-[Version].tar.gz
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
To Be Update
