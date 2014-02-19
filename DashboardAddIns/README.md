# Dispatcher application

Dispatcher application for Windows, build as ArcGIS Operations Dashboard Add-Ins.

![App](dispatcher-app.png?raw=true)

## Requirements
* [Operations Dashboard for ArcGIS](http://www.esri.com/software/arcgis/arcgisonline/apps/operations-dashboard)
* [ArcGIS 10.2 Runtime SDK for WPF](http://resources.arcgis.com/en/communities/runtime-wpf) (includes the Operations Dashboard for ArcGIS SDK)
* Visual Studio 2010 or above
* .Net Framework 4.0 or above

## Building the source code:
1. Make sure that Visual Studio 2010 or later is installed.
2. Make sure that the Operations Dashboard for ArcGIS is installed.
3. Make sure that ArcGIS 10.2 Runtime SDK for WPF is installed
4. Open the RouteMonitor.sln solution file in Visual Studio and make sure all the project references are resolved.
5. Build the solution.

## Installing the Operations Dashboard Add-Ins:
1. Locate the built "Stops.opdashboardAddin" & "GeoFences.opdashboardAddin" add-in files. The default output location is C:\ArcGIS\DotNet\RouteMonitor\Bin.
2. Login to arcgis.com with your organizational account, or to your ArcGIS Portal, and upload the two add-ins via the "MY CONTENT" page.
3. Run Operations Dashboard for ArcGIS and follow the manual on how to use the add-ins with your Operations View.
