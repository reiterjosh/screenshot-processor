- Instructions to use the csv tool -

- Setup

* navigate to createHashCSV directory
cd /Users/google/AndroidAuto/csvtool/

* compile the .java file to a .class file so it can be run
javac createHashCSV.java



- Run

java createCSVHash -f [format_string] -d [screensDirectory]

* -f followed by an underscore separated string lets the tool know the format of the screen filenames
* the only required pieces of the format and filename are locale, script, and name
* if the script is missing you can specify it when the csv tool is run in the format string
	eg locale_name_script=ScriptName

* -d followed by a directory shows the tool where the screens to be processed are located
* if the directory has spaces, it needed to be surrounded in quotes
	eg “/Users/google/Desktop/Processed/NYC_C1_Home screen”

* open a csv file afterward to make sure the names and script look okay
* if not, you can rename source files if needed, and delete the output folders and run it again


- Output Folders

csv		-> contains one csv file per locale
hashed		-> contains the hashed images (may total than number of source images)
duplicates	-> contains log of duplicate images when creating hash files



- Examples

* for screens with names like "en-US_Calculator_cannot-divide-by-zero"
java createHashCSV -f locale_script_name -d "/Users/google/Desktop/Processed"

* "bs-BA-Cyrl_000_NYC-C1_Contacts_01"
java createHashCSV -f locale_index_build_name_index_script=Contacts -d "/Users/google/Desktop/Processed/Contact screens"


- Notes on filenames
* script names should be CamelCase with no spaces, eg ‘DataUsage’
* screen names should be dash separated
* if a screen name is non descriptive and is in the format script-index it should be script_index, because the csvtool has to add an index to the name anyway
	eg en_Calculator_Calculator-01 -> en_Calculator_Calculator_01 or en_Calculator_01