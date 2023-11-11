package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MessageFactory {
    // Matches (ip|hostname):message
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^(.+):(.+)$");

    // Matches (ip|hostname):file=/path/to/file
    private static final Pattern FILE_MESSAGE_PATTERN = Pattern.compile("^(.+):file=(.+)$");

    public static Message build (String input) throws FileNotFoundException, InvalidInputException {
        var matcher = MessageFactory.FILE_MESSAGE_PATTERN.matcher(input);
        if (matcher.matches()) {
            var hostname = matcher.group(1);
            var filePath = matcher.group(2);
            Path path = Paths.get(filePath);
            List<String> lines;

            try {
                var filename = path.getFileName().toString();
                try (var stream = Files.lines(path)) {
                    lines = stream.toList();
                }

                return new Message(
                        "",
                        hostname,
                        filename,
                        lines
                );
            } catch (IOException e) {
                throw new FileNotFoundException(String.format("SYSTEM: File %s not found.\n", path.getFileName().toString()));
            }
        }

        matcher = MessageFactory.MESSAGE_PATTERN.matcher(input);
        if (matcher.matches()) {
            var hostname = matcher.group(1);
            var messageContent = matcher.group(2);

            return new Message(
                    "",
                    hostname,
                    messageContent,
                    new ArrayList<>()
            );
        }

        throw new InvalidInputException("SYSTEM: Input must be in format ip:message OR ip:file=/path/to/file !\n");
    }
}
