# Integration Examples

Some example routes.  More examples routes will be added once more integration engine functionality is developed.

> **Note:** Route and component records are written to the database on application startup. This occurs only the first time the app runs.

---

### **hl7-file-input-route**
Reads a file from a directory and passes it directly to an outbound route connector.  An outbound route connector does not know what routes (if any) will receive the message.

---

### **hl7-file-output-route**
Receives messages from two other routes via two inbound route connectors and writes them to a directory.

---

### **mllp-input-route**
Receives a HL7 message via MLLP and passes it directly to an outbound route connector.  An outbound route connector does not know what routes (if any) will receive the message.

---

### **mllp-output-route**
Contains two routes with various message handler types (transformation, splitter, filter).