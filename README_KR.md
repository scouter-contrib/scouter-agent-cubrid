[![English](https://img.shields.io/badge/language-English-orange.svg)](README.md) [![Korean](https://img.shields.io/badge/language-Korean-blue.svg)](README_KR.md)
# SCOUTER-AGENT-CUBRID
CUBRID DBMS를 모니터링 하기 위한 Agent (https://github.com/CUBRID/cubrid)

 * 중요
   - 모든 기능을 사용하기 위해서는 CUBRID 10.2.1 버전이상이 필요합니다.
   - Scouter 2.15.0 버전부터 지원합니다.
     (https://github.com/scouter-project/scouter/releases/tag/v2.15.0)

## SCOUTER-AGENT-CUBRID를 빌드하는 방법

### 필요 요소

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
- pom.xml 파일을 import하여 maven 프로젝트에 추가한다.
- scouter-common.jar이 필요하므로 maven 빌드 후 eclipse 빌드를 사용한다.

##### 결과물 정보
- Package File : SourcePath/target/package/scouter-agent-cubrid-[Version].tar.gz
- Excute Files : SourcePath/target/scouter-cubrid-agent

## PORT 정보 (기본값)

- CUBRID Agent UDP : 6200 
- CUBRID Server HTTP : 8001
- SCOUTER Server(Collector) UDP : 6100
- SCOUTER Sserver(Collector) TCP : 6100

### conf/scouter_cubrid.conf을 통해 아래와 같이 설정
```
net_local_udp_ip=127.0.0.1  # Agent IP
net_local_udp_port=6200     # Agent UDP
net_collector_ip=127.0.0.1  # SCOUTER Server IP
net_collector_udp_port=6100 # SCOUTER Server UDP Port
net_collector_tcp_port=6100 # SCOUTER Server UDP Port
cubrid_cms_ip=127.0.0.1     # Cubrid Manager Server IP
cubrid_cms_port=8001        # Cubrid Manager Server Port
```
## 사용자 가이드
- [Quick Start](documents/quick_start.md)
- [Client UI Guide](documents/client_guide.md)

