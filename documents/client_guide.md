# Client UI Guide

## **- Connect**

<img src="images/guide/ui_g_1.png"  width="30%" height="30%"/>

## **- Check the CUBIRID agent Connection status**

<img src="images/guide/ui_g_2.png"  width="30%" height="30%"/>

When the CUBRID Agent is connected to a server, an object is added to the Objects View as shown in the figure above.
The CUBRID Agent connection is checked under the HostName where the Agent is executed and the subordinate.

## **- Add CUBRID Perspective**

You can add a perspective to check the CUBRID information.

<img src="images/guide/ui_g_3.png"  width="40%" height="40%"/>

* Initialization screen

<img src="images/guide/ui_g_4.png"  width="80%" height="80%"/>


## **- Add View**

* After selecting the Cubrid Object in the ObjectView, through the Context Menu and MainMenu(Object)
You can add more views to the initial perspective.

<img src="images/guide/ui_g_va_1.png"  width="50%" height="50%"/>
<img src="images/guide/ui_g_va_2.png"  width="50%" height="50%"/>
<img src="images/guide/ui_g_va_3.png"  width="50%" height="50%"/>


* A View with a fixed DB and Counter is added as a Context Menu to each View.
DB, Counter selection is fixed and maintained even when restarting.

<img src="images/guide/ui_g_vi_4_add.png"  width="50%" height="50%"/>


## **- View information**

### **1) Server Info**

<img src="images/guide/ui_g_vi_1.png"  width="50%" height="50%"/>

CUBRID DB information connected to CMS, CPU usage in DB, ACTIVE SESSION,
The LOCK WAIT SESSIONS information is displayed, and the view cannot be closed and can only be minimized.

```
* CPU(%) : CPU usage for DB. 
* ACTIVE SESSION : Sum of Active TranList connected to each DB. (gettransactioninfo)
* LOCK WAIT SESSIONS : Sum of TranList where wait_for_lock_holder is not (-1) in each DB. (gettransactioninfo )
```

### **2) SingleRealTimeMultiView**

<img src="images/guide/ui_g_vi_2.png"  width="50%" height="50%"/>

View that displays a single item among DB information and broker information as a graph.

#### **Single item list**

1. Same information as ServerInfo information

```
  * Cpu Usage : CPU usage for DB.
  * Active Sessions : Sum of Active TranList connected to each DB. 
  * Lock Wait Sessions : Sum of TranList where wait_for_lock_holder is not (-1) in each DB.
```

2. list of StatDump info (Num values ​​that can be checked through CUBRID statdump util)
```
Data Page IO Writes
Data Page IO Reads
Data Page Fetches
Data Page Dirties
Data Buffer Hit Ratio
Query Sscans
Sort IO Page
Sort Data Page
```

3. Processed plandump info (through CUBRID plandump util)
```
XASL Plan Hit Rate (%) : Divide Hits value by Lookups value in plandump XASL Cache.
Filter Predicate Hit Rate (%) : Divide EntryHists value by Lookups value in plandump Filter Predicate Cache 
```

4. list of broker info
```
Transaction Per 5 Second : Cumulative data for 5 seconds in TPS of Broker status info .
Query Per 5 Second : Cumulative data for 5 seconds in QPS of Broker status info .
Error Query Per 5 Second : Cumulative data for 5 seconds in Error Query of Broker status info .

```

#### **View for Realtime and Past through context menu in SingleRealTimeMultiView**

<img src="images/guide/ui_g_vi_4_add.png"  width="50%" height="50%"/>

You can add RealTime View and Past View to check real-time data or past data.

1. Viewer for realtime : Real-time information transferred every 5 seconds from CMS(CUBRID Manager Server).

[popup for add View]

<img src="images/guide/ui_g_vi_5_real1.png"  width="50%" height="50%"/>


[Result]

<img src="images/guide/ui_g_vi_5_real2.png"  width="50%" height="50%"/>

2. Viewer for Past – Less than a day : Set to less than one day and real-time information display period data as a graph.

[popup for add View]

<img src="images/guide/ui_g_vi_6_spast1.png"  width="50%" height="50%"/>


[Result]

<img src="images/guide/ui_g_vi_6_spast2.png"  width="50%" height="50%"/>

3. Viewer for Past – More then a day : Set to more than one day and Accumulate real-time information to display period data as a graph

[popup for add View]

<img src="images/guide/ui_g_vi_7_lpast1.png"  width="50%" height="50%"/>


[Result]

<img src="images/guide/ui_g_vi_7_lpast2.png"  width="50%" height="50%"/>


### **3) DB Space Info**
View showing volume capacity information in each DB

<img src="images/guide/ui_g_vi_8_space.png"  width="50%" height="50%"/>

### **4) Long Transaction List**

Transaction information that has timed out for more than 3 seconds is updated after the view is created.
If the host, pid, user, and program are different even in the same SQL text, it is a different transaction.
MaxList can be changed and data can be checked from 10 to 1000.

<img src="images/guide/ui_g_vi_9_transaction.png"  width="50%" height="50%"/>