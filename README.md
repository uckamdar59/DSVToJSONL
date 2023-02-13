# DSVToJSONL
To run the jar file, we need to run the following command in the terminal. It takes 3 arguments which must be passed to run the application.

`java -jar DSVToJSONLConverter-1.0-SNAPSHOT-jar-with-dependencies.jar <input_file_path> <delimiter> <output_file_path>`

##### Ex:
`java -jar DSVToJSONLConverter-1.0-SNAPSHOT-jar-with-dependencies.jar "/Users/umang/Desktop/umang/input.txt" "," "/Users/umang/Desktop/umang/output.jsonl"`

This command will run the jar file and will convert the input file to the required output .jsonl file. On successful completion, it will print the below message
`File converted successfully!!!😁`

Note: Jar file is already present in the target folder, to create a new file, use `mvn package` command

