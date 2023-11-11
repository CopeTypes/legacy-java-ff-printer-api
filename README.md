# ff-printer-api
Java API for communicating with network-enabled FlashForge printers, focused mainly on the Adventurer 5M.


Can be easily integrated with a failure detection API like [PrintWatch](https://printpal.io/printwatch/) to automatically stop failing prints while you're away.

# Printer Compatability
 - Adventurer 5M/5M Pro (Fully Compatible)
 - Adventurer 4 (No, planned)
 - Adventurer 3 (*Should work*, but code changed to be compatible with the 5 might need work. Full support planned)
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
