## How does this integrate with Tasker/Llama or other automation frameworks?

## How to automate Formmicro?

### Controlling Formmicro

If your automation app can send intents, you can use those intents to control Formmicro and get it to perform a few actions.

To invoke it from Tasker, create a new action under Misc > Send Intent. 

>Action: `com.formmicro.gpslogger.GpsLoggingService`
Extra: `immediatestart:true (others below)`  
Target: `Service`


To invoke it from your own Android code:

    Intent i = new Intent("com.formmicro.gpslogger.GpsLoggingService");
    i.setPackage("com.formmicro.gpslogger");
    i.putExtra("immediatestart", true);
    startService(i);


**These are the extras you can send to Formmicro**:

>`immediatestart` - (true/false) Start logging immediately  

> `immediatestop` - (true/false) Stop logging

> `setnextpointdescription` - (text) Sets the annotation text to use for the next point logged

> `settimebeforelogging` - (number) Sets preference for logging interval option  

> `setdistancebeforelogging` - (number) Sets preference for distance before logging option

> `setkeepbetweenfix` - (true/false) Sets preference whether to keep GPS on between fixes

> `setretrytime` - (number) Sets preference for duration to match accuracy

> `setabsolutetimeout` - (number) Sets preference for absolute timeout
  
> `setprefercelltower` - (true/false) Enables or disables the GPS or celltower listeners

> `logonce` - (true/false) Log a single point, then stop

> `switchprofile` - (text) The name of the profile to switch to

> `getstatus` - (true) Asks Formmicro to send its current events broadcast

### Shortcuts

The app comes with a Start and a Stop **shortcut** (long press home screen, add widget), you can invoke those from some automation apps.


### Formmicro Events Broadcast

### Listening to Formmicro


(Experimental feature) Formmicro sends a broadcast start/stop of logging, which you can receive as an event.
  
In Tasker, this would look like:  
  
> Event: Intent Received  
  Action: com.formmicro.gpslogger.EVENT
  
From there in your task, you can look at the following variables
 
 * `%formmicrologgerevent` - `started` or `stopped`
 * `%filename` - the base filename that was chosen (no extension)
 * `%startedtimestamp` - timestamp when logging was started (epoch)

In a custom application, receive the `com.formmicro.gpslogger.EVENT` broadcast and have a look inside the extras.