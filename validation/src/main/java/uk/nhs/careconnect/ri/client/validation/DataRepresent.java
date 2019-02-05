package uk.nhs.careconnect.ri.client.validation;

import lombok.Data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DataRepresent {

    private List<ResourceStatus> resourceStatusList;
    private ServerStatus serverStatus;

    public void represent() {
        String table = new StringBuffer()
                .append("<table>")
                .append(buildServerStatus(serverStatus))
                .append(resourceStatusList.stream().map(this::mapTable).collect(Collectors.joining( "")))
                .append("</table>")
                .toString();
        writeToFile(table);
//        System.out.println("table:" + table);
    }

    private String buildServerStatus(ServerStatus serverStatus) {
        return new StringBuffer()
                .append("<tr><td colspan=4><b>Server Status</b></td></tr>")
                .append("<tr>")
                .append("<td><b>Publisher</b></td><td colspan=3>")
                .append(serverStatus.getPublisher())
                .append(" (")
                .append(serverStatus.isPublisherSpecified())
                .append(")</td></tr>")
                .append("<tr>")
                .append("<td><b>Version</b></td><td colspan=3>")
                .append(serverStatus.getVersion())
                .append(" (")
                .append(serverStatus.isVersionAvailable())
                .append(")</td></tr>")
                .append("<tr>")
                .append("<td><b>Status</b></td><td colspan=3>")
                .append(serverStatus.getStatus())
                .append(" (")
                .append(serverStatus.isStatusActive())
                .append(")</td></tr>")
                .append("<tr>")
                .append("<td><b>Formats</b></td><td colspan=3>")
                .append(serverStatus.getFormats())
                .append(" (")
                .append(serverStatus.isValidFormat())
                .append(")</td></tr>")
                .toString();
    }

    private void writeToFile(String table) {
        Path path = Paths.get("./target/table.html");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String mapTable(ResourceStatus resourceStatus) {
        String trs = resourceStatus.getInteractionStatusList().stream().map(this::mapTr).collect(Collectors.joining( ""));
        return new StringBuffer()
                .append("<tr><td><b>")
                .append(resourceStatus.getName())
                .append("</b></td>")
                .append("<td colspan=3><b>Profile Status (")
                .append(resourceStatus.getProfileStatus())
                .append(")</b></td></tr>")
                .append(trs)
                .toString();
    }

    private String mapTr(InteractionStatus interactionStatus) {
        return new StringBuffer()
                .append("<tr><td><b>")
                .append(interactionStatus.getInteractionName())
                .append("</b></td><td>")
                .append(interactionStatus.getStatus())
                .append("</td><td colspan=2>")
                .append(interactionStatus.getData())
                .append("</td></tr>")
                .append(interactionStatus.getSubInteraction().stream().map(this::mapSubTr).collect(Collectors.joining("")))
                .toString();
//        interactionStatus.getSubInteraction().stream().map(this::mapSubTr).collect(Collectors.joining(""));
//        return sb.toString();

    }

    private String mapSubTr(InteractionStatus interactionStatus) {
        return new StringBuffer()
                .append("<tr><td>")
                .append(interactionStatus.getInteractionName())
                .append("</td><td>")
                .append(interactionStatus.getStatus())
                .append("</td><td colspan=2>")
                .append(interactionStatus.getData())
                .append("</td></tr>")
                .toString();
    }
}
