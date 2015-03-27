# phoenix
=====================================================================
Requires root!
=====================================================================
You need to grant permission when your SU app requests
=====================================================================
An application to reboot your phone in a given interval on a specific time and start a specific application after reboot.
The idea is to restart Android and an application periodically to clean up cache and memory leaks if the application (like a security cam >> see Alfred in GooglePlay) has to run over a long time.

And if you don't need to restart an application, still this can help to close memory leaks.
No background services - reboot is scheduled by system AlarmManager.
Before reboot is applied, all apps get a shutdown call to save there data. This helps to prevent data loss!

Configuration allows
- selection of reboot interval from every day to every 30 days
- selection of reboot time from midnight to 11pm
- selection between hard reboot (complete restart) or soft/hot reboot (only restart apps)
- selection if you want to start an app automatically after reboot
- selection of any app to be started automatically after reboot
