# Phoenix
=====================================================================
### Requires root! Requires Android 4.0.3 or newer!
=====================================================================
### You need to grant permission when your SU app requests
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

[Video](https://www.youtube.com/watch?v=Hdcz53Tt44A)

[Screenshot 1](https://lh3.googleusercontent.com/Wa7sxxaOC8aErUhtrgnMrdTGL0zSvO8VhW2vwFEoaV-7yoldOlCWlk20DWA48sGaOg=h310-rw)

[Screenshot 2](https://lh3.googleusercontent.com/qVvuqH-TtW0IXwn-TK8XrHcnUVLGqxPVWLRQSUpM5wiX5_McRXVX1xh-fTkl6JDowFcr=h900-rw)

[Screenshot 3](https://lh3.googleusercontent.com/CGm92sk5Ioat857U2L_rtMTFlROv7fxDfio19kOChHwaRsYEyy6amtzzZxy2xKzowXcw=h900-rw)

[Screenshot 4](https://lh3.googleusercontent.com/TeoRetUPWBFeKclDC9T0Z-AK5Qv58VDEjBOOK8DGXcnx74vttjJtKEzguzCLWQj7Qg=h900-rw)

[Google PlayStore](https://play.google.com/store/apps/details?id=tk.giesecke.phoenix)
