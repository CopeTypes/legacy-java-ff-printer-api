# ff-printer-api
Legacy Java API for communicating with network-enabled FlashForge printers, focused mainly on the Adventurer 5M.

#  This library is no longer maintained due to a port to C#.<br> On new firmware (using Orca-FlashForge), the 5M (and possibly others?) use a new API for communication.<br> The C# API is available [here](https://github.com/CopeTypes/ff-5mp-api)

## This library should continue to work as long as Flashprint is maintained, as they keep both the new and old API in the latest firmware(s) for compatibility.

# Printer Compatability
 - Adventurer 5M/5M Pro (Fully Compatible - Tested up to firmware 2.7.5 at the time of archiving)
 - Adventurer 4 (No, no testing)
 - Adventurer 3 (Unsure, no testing)
 - Other printers TBA. I only have an Adventurer 5M Pro, and a Creator Pro 2.

# Features
 - Sending prints & starting/stopping prints
 - Getting current XYZ coordinate(s)
 - Getting extruder / build plate temp
 - Getting print progress (sd byte progress/layer progress)
 - Getting endstop information (Machine/Move status, current file name if printing, etc)
 - Getting printer information (name, firmware version, mac, etc)
 - Sending custom MCode command (compatibility for what does/doesn't work varies between printers/firmwares)
 - Basic discord webhook functionality (can easily be improved/changed to suit your needs)

# Credits
[Slugger2k](https://github.com/Slugger2k) for a good chunk of the base code. (This project uses a modified version of the [FlashForgePrinterApi](https://github.com/Slugger2k/FlashForgePrinterApi))
