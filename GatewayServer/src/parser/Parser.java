package parser;

import com.google.gson.JsonObject;
import java.nio.ByteBuffer;

public interface Parser {
    JsonObject parse(ByteBuffer input);

//    ByteBuffer serialize(JsonObject response);

    }
