package featurecat.lizzie.analysis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface GtpCommand {
    default GtpFuture play(String color, String move) {
        if (StringUtils.equalsIgnoreCase(color, "B") || StringUtils.equalsIgnoreCase(color, "W")) {
            return postCommand(String.format("play %s %s", color, move));
        } else {
            throw new IllegalArgumentException("The stone color must be BLACK(B) or WHITE(W), but is " + color);
        }
    }

    default GtpFuture undo() {
        return postCommand("undo");
    }

    default GtpFuture clearBoard() {
        return postCommand("clear_board");
    }

    default List<String> sendCommand(String command) {
        GtpFuture commandFuture = postCommand(command);
        try {
            return commandFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            return Collections.emptyList();
        }
    }

    GtpFuture postCommand(String command);

    static boolean isSuccessfulResponse(List<String> response) {
        boolean result = false;

        if (CollectionUtils.isNotEmpty(response)) {
            String beginElement = response.get(0);
            if (beginElement.startsWith("=")) {
                result = true;
            }
        }

        return result;
    }

    static String getLineWithoutResponseHeader(List<String> response, int lineIndex) {
        if (lineIndex < 0 || lineIndex >= (response == null ? -1 : response.size())) {
            return "";
        }
        String line = StringUtils.defaultString(response.get(lineIndex));
        if (lineIndex == 0) {
            return StringUtils.removeFirst(line, "^[=?]\\d*\\s*");
        } else {
            return line;
        }
    }

    static List<String> removeResponseHeaderInPlace(List<String> response) {
        if (CollectionUtils.isNotEmpty(response)) {
            String beginElement = response.get(0);
            if (beginElement.startsWith("=") || beginElement.startsWith("?")) {
                beginElement = StringUtils.removeFirst(beginElement, "^[=?]\\d*\\s*");
                response.set(0, beginElement);
            }
        }

        return response;
    }

    static List<String> removeResponseHeaderCopy(List<String> response) {
        return removeResponseHeaderInPlace(Lists.mutable.ofAll(response));
    }

    static MutableIntList parseResponseIntTable(List<String> response) {
        MutableIntList result = new IntArrayList();
        for (String lineString : response) {
            String[] line = lineString.split("\\s+");
            for (String lineItemString : line) {
                try {
                    int lineItem = Integer.parseInt(lineItemString);
                    result.add(lineItem);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        return result;
    }

    static MutableDoubleList parseResponseDoubleTable(List<String> response) {
        MutableDoubleList result = new DoubleArrayList();
        for (String lineString : response) {
            String[] line = lineString.split("\\s+");
            for (String lineItemString : line) {
                try {
                    double lineItem = Double.parseDouble(lineItemString);
                    result.add(lineItem);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        return result;
    }
}
