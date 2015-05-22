# osmosis-calculator-plugin
OSMOSIS plugin that can do math with the Node tag values. Use at your own risk. 

It uses exp4j ([http://www.objecthunter.net/exp4j/](http://www.objecthunter.net/exp4j/ "exp4j")) to calculate like the user wants it to.

## Usage ##
### Library usage ###
You can use the plugin as a library like you would use every other OSMOSIS plugin. Just build it and use it, at your own risk. See the javadoc for more information.

### Usage from the CLI ###
If you want to use the plugin with OSMOSIS directly from the command line, build it and put its `-jar-with-dependencies.jar` file into OSMOSIS' `bin/plugins` folder. 

#### Command line parameters ####

The task name for this plugin is `calculate-node-tag`.
 
- `calculation` The mathematical expression to be calculated. Everything that exp4j understands should be supported.
- `outputTag` The tag where the result value will be stored.
- `inputTags` Provide the tag names which will be used as variables in your calculation (comma-separated). **Although not being tags, lat and lon are also provided as variables**.
- `removeTags` The tags that will be removed after the calculation  (comma-separated).

#### Examples ####
- Rename the `height` tags to `ele`:

        osmosis --read-xml inputFile.osm --calculate-node-tag inputTags=height calculation=height outputTag=ele removeTags=height --write-xml outputFile.osm

- Just remove the `ele` and `height` tags:

        osmosis --read-xml inputFile.osm --calculate-node-tag removeTags=height --write-xml outputFile.osm

- Calculate examplary nonsense (average of lat and lon):

        osmosis --read-xml inputFile.osm --calculate-node-tag inputTags=lat,lon calculation=(lat+lon)/2 outputTag=foo --write-xml outputFile.osm

## Versions ##

        v1.0: First version

## Thanks to... ##

- OSMOSIS developers
- exp4j developers
- Franz Graf (structure copied from his SRTM plugin)