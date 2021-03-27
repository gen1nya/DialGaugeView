# ArrivalTest-DialGaugeView

Simple dial gauge view for android. 

May be added to project via git submodule or as AAR file (run 'assemble' gradle task in dialGaugeView module)

## Using


add ```DialGaugeView``` in xml layout file:

``` xml
<com.android.dialgaugeview.DialGaugeView
        android:id="@+id/vTachometer"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:dgv_digits_margin="50dp"
        app:dgv_end_value="10000"
        app:dgv_notches="11"
        app:dgv_start_angle="130"
        app:dgv_sweep_angle="280" />
```


### Customizable parameters: 

```dgv_digits_margin``` - space between digits and scale

```dgv_end_value``` - maximum scale value. minimum value is always 0

```dgv_notches``` - number of scale notches

```dgv_start_angle``` - scale start point (in degrees)

```dgv_sweep_angle``` - scale arc degree

```dgv_text_size``` - text size ;)


### Display data:

for set data to view use the method:
``` kotlin
setData(data: Double)
```

it ignores anything out of range ```0..end_value```

## Demo app

Demo project contains two applications:

```client``` - single activity application with ViewPager and two dial guage views. 
For changing view use two-finger scroll

```server``` - simple service, generates new data every 1 ms by sum few sinus functions with different coefficients

For communicate between client and server and transfer generated data uses AIDL and service binding.
Aidl files have been moved to a special module to make it easier to work with it from different modules (client and server)

### Running

from IDE: run configuration ```vehicleDataService``` and then ```app```. This configuration build two modules and install its.

from APK: you must build two APKs (for ```app``` and ```vehicleDataService``` modules) and install both. First - service and then client

