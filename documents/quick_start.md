# Quick Start Guide
To monitor CUBRID from scouter, Scouter Server, Client, and Agent installation and configuration are required.

Installation and setup are simple described below.

---
## Souter Server 

#### 1. File download. 
- Scouter Version 2.15.0+
- https://github.com/scouter-project/scouter/releases/tag/v2.15.0

#### 2. Decompress the downloaded file.
<p>decompress to any directory you want to.</p>
<img src="images/quick_start/server_1.png"  width="50%" height="50%"/>

#### 3. Run the scouter server(Collector)
- Linux
```
startup.sh
```
- Windows
```
startup.bat
```

- Executed Result
<img src="images/quick_start/server_2.png"  width="40%" height="40%"/>

#### For detailed Setup Guide, please refer to Setup.md or document provided by Scouter.
https://github.com/scouter-project/scouter/blob/master/scouter.document/main/Setup.md

---
## Scouter-Cubrid-Agent
#### 1. File Download 
- https://github.com/scouter-contrib/scouter-agent-cubrid/releases

#### 2. Decompress the downloaded file.
<p>decompress to any directory you want to.</p>
<img src="images/quick_start/agent_1.png"  width="60%" height="60%"/>

#### 3. Connect Configuration
- /conf/scouter_cubrid.conf
```
net_local_udp_ip=127.0.0.1  # Agent IP
net_local_udp_port=6200     # Agent UDP
net_collector_ip=127.0.0.1  # SCOUTER Server IP
net_collector_udp_port=6100 # SCOUTER Server UDP Port
net_collector_tcp_port=6100 # SCOUTER Server UDP Port
cubrid_cms_ip=127.0.0.1     # Cubrid Manager Server IP
cubrid_cms_port=8001        # Cubrid Manager Server Port
```
#### 4. Run the CUBRID agent
- Linux
```
startup.sh [cms_user_id] [cms_user_password] [dba_user_id] [dba_user_password]
```
- Windows
```
startup.bat [cms_user_id] [cms_user_password] [dba_user_id] [dba_user_password]
```

- Executed Result

(Success)

<img src="images/quick_start/agent_2.png"  width="30%" height="30%"/>

(Fail)

<img src="images/quick_start/agent_3.png"  width="30%" height="30%"/>

#### 5. Stop the CUBRID agent
- Linux
```
stop.sh
```
- Windows
```
stop.bat
```

---

## Scouter Client

#### 1. File Download 
- https://github.com/scouter-project/scouter/releases

#### 2. Decompress the downloaded file.
<p>decompress to any directory you want to.</p>
<img src="images/quick_start/client_1.png"  width="60%" height="60%"/>

#### 3. Run the scouter client
Click the scouter client execution file(scouter.exe) and run.
On the login form, you can login with [server_ip]:[server_port] for collector server and admin/admin for default id/password.

<img src="images/quick_start/client_2.png"  width="20%" height="20%"/>

#### 4. Open CUBRID Perspective

<img src="images/quick_start/client_3.png"  width="60%" height="60%"/>

---
