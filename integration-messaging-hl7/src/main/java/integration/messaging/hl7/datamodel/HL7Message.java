package integration.messaging.hl7.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.SerializationUtils;

/**
 * An object to store the segments and fields of a HL7 messages. Provides
 * methods to manipulate the message and reconstruct it.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7Message implements Serializable {
    private static final long serialVersionUID = -2986106720527032903L;

    private List<Segment> segments = new ArrayList<Segment>();

    public HL7Message(String sourceHL7Message) {

        String[] splitMessageSegments = sourceHL7Message.split("\r(?=[A-Za-z0-9]{3}|)");

        for (String value : splitMessageSegments) {
            Segment segment = createSegment(value);

            segments.add(segment);
        }
    }

    /**
     * Returns the type of the message. MSH-9.
     * 
     * @return
     * @throws Exception
     */
    public Field getMessageTypeField() throws Exception {
        return getMSHSegment().getMessageTypeField();
    }

    /**
     * Does the supplied segment exist?
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public boolean doesSegmentExist(String segment) throws Exception {
        return getSegmentCount(segment) > 0;

    }

    /**
     * Returns the MSH segment.
     * 
     * @return
     */
    public MSHSegment getMSHSegment() {
        for (Segment segment : this.segments) {
            if (segment.getName().equals("MSH")) {
                return (MSHSegment) segment;
            }
        }

        return (MSHSegment) createSegment("MSH");
    }

    /**
     * Returns the PID segment.
     * 
     * @return
     */
    public PIDSegment getPIDSegment() {
        for (Segment segment : this.segments) {
            if (segment.getName().equals("PID")) {
                return (PIDSegment) segment;
            }
        }

        return (PIDSegment) createSegment("PID");
    }

    /**
     * Gets all segments.
     * 
     * @return
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * Set all segments.
     * 
     * @param segments
     */
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    @Override
    public String toString() {
        return segments.stream().map(Segment::toString).collect(Collectors.joining("\r"));
    }

    /**
     * Removes an occurrence of a segment.
     * 
     * @param name
     * @param occurrence
     */
    public void removeSegment(String name, int occurrence) throws Exception {

        int currentOccurrence = 0;

        for (Iterator<Segment> iter = getSegments().iterator(); iter.hasNext();) {
            Segment segment = iter.next();

            if (segment.getName().equals(name)) {
                if (currentOccurrence == occurrence) {
                    iter.remove();
                    return;
                }

                currentOccurrence++;
            }
        }
    }

    /**
     * Removes the segment at the supplied rowIndex.
     * 
     * @param rowIndex
     */
    public void removeSegment(int rowIndex) throws Exception {
        if (rowIndex >= segments.size()) {
            return;
        }

        this.segments.remove(rowIndex);
    }

    /**
     * Removes the supplied segment from the array of segments.
     * 
     * @param segment
     * @throws Exception
     */
    public void removeSegment(Segment segment) throws Exception {
        this.segments.remove(segment);
    }

    public void removeSegments(Segment[] segments) throws Exception {

        for (Segment segment : segments) {
            removeSegment(segment);
        }
    }

    /**
     * Removes the supplied segment from the array of segments.
     * 
     * @param segments
     * @throws Exception
     */
    public void removeSegments(List<Segment> segments) throws Exception {

        for (Segment segment : segments) {
            removeSegment(segment);
        }
    }

    /**
     * Removes the 1st occurrence of a segment.
     * 
     * @param name
     */
    public void removeSegment(String name) throws Exception {
        removeSegment(name, 0);
    }

    /**
     * Gets the segment at the supplied index.
     * 
     * @param segmentIndex
     * @return
     */
    public Segment getSegment(int rowIndex) {
        if (rowIndex >= segments.size()) {
            return null;
        }

        return segments.get(rowIndex);
    }

    /**
     * Returns a count of the number of segments matching the supplied segment name.
     * 
     * @param message
     * @param segmentName
     * @return
     */
    public int getSegmentCount(String segmentName) throws Exception {
        int segmentCount = 0;

        for (Segment segment : getSegments()) {
            if (segment.getName().equals(segmentName)) {
                segmentCount++;
            }
        }

        return segmentCount;
    }

    /**
     * Returns the index of a matching segment.
     * 
     * @param message
     * @param segmentName
     * @param occurrence
     * @return
     * @throws Exception
     */
    public Integer getSegmentIndex(String segmentName, int occurrence) throws Exception {
        List<Integer> segmentIndexes = getSegmentIndexes(segmentName);

        if (segmentIndexes.isEmpty()) {
            return null;
        }

        if (occurrence > segmentIndexes.size()) {
            return null;
        }

        return segmentIndexes.get(occurrence);
    }

    /**
     * Returns the row indexes of segments which match the supplied name.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public List<Integer> getSegmentIndexes(String segmentName) throws Exception {
        List<Integer> segmentIndexes = new ArrayList<>();

        for (int i = 0; i < getSegments().size(); i++) {
            Segment segment = getSegment(i);

            if (segment.getName().equals(segmentName)) {
                segmentIndexes.add(i);
            }
        }

        return segmentIndexes;
    }

    /**
     * Returns the first matching segment.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public Integer getFirstSegmentIndex(String segmentName) throws Exception {
        return getSegmentIndex(segmentName, 0);
    }

    /**
     * Copies a segment.
     * 
     * @param message
     * @param sourceIndex
     * @param targetIndex
     * @throws Exception
     */
    public void copySegment(int sourceIndex, int targetIndex) throws Exception {
        if (sourceIndex >= getSegments().size()) {
            return;
        }

        Segment sourceSegment = getSegments().get(sourceIndex);

        getSegments().set(targetIndex, SerializationUtils.clone(sourceSegment));
    }

    /**
     * Inserts a segment at the target index which has identical content to the
     * source segment.
     * 
     * @param message
     * @param sourceIndex
     * @param targetIndex
     * @throws Exception
     */
    public Segment insertSegment(Segment source, int targetIndex) throws Exception {
        Segment segment = createSegment(source.toString());

        getSegments().add(targetIndex, segment);

        return segment;
    }

    /**
     * Moves a segment from one location to another. If the newIndex is not in range
     * then the segment is appended to the end.
     * 
     * @param message
     * @param currentIndex
     * @param newIndex
     * @throws Exception
     */
    public void moveSegment(int currentIndex, int newIndex) throws Exception {
        if (currentIndex >= getSegments().size()) {
            return;
        }
        boolean append = false;
        Segment sourceSegment = getSegments().get(currentIndex);
        getSegments().remove(sourceSegment);
        if (newIndex >= getSegments().size()) {
            append = true;
        }
        if (append) {
            getSegments().add(sourceSegment);
        } else {
            getSegments().add(newIndex, sourceSegment);
        }
    }

    /**
     * Moves the segment at the supplied index to the end of the message.
     * 
     * @param currentIndex
     * @throws Exception
     */
    public void moveSegmentToEnd(int currentIndex) throws Exception {
        moveSegment(currentIndex, getSegments().size() + 1);
    }

    /**
     * Moves the segment to the end of the message.
     * 
     * @param currentIndex
     * @throws Exception
     */
    public void moveSegmentToEnd(Segment segment) throws Exception {
        moveSegmentToEnd(getSegmentIndex(segment));
    }

    /**
     * Insert a segment.
     * 
     * @param message
     * @param newSegmentName
     * @param segmentIndex
     * @param id
     * @throws Exception
     */
    public Segment insertSegment(String newSegmentName, int segmentIndex, int id) throws Exception {
        Segment segment = createSegment(newSegmentName + "|" + id);

        getSegments().add(segmentIndex, segment);

        return segment;
    }

    /**
     * Appends a segment to the end of the message.
     * 
     * @param newSegmentName
     * @param id
     * @return
     * @throws Exception
     */
    public Segment appendSegment(String newSegmentName, int id) throws Exception {
        Segment segment = createSegment(newSegmentName + "|" + id);

        getSegments().add(segment);

        return segment;
    }

    /**
     * Returns all matching segments.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public List<Segment> getSegments(String segmentName) throws Exception {
        List<Segment> segments = new ArrayList<>();

        for (Segment segment : getSegments()) {
            if (segment.getName().equals(segmentName)) {
                segments.add(segment);
            }
        }

        return segments;
    }

    /**
     * Returns the row index of the supplied segment.
     * 
     * @param message
     * @param segment
     * @return
     * @throws Exception
     */
    public Integer getSegmentIndex(Segment segment) throws Exception {

        for (int i = 0; i < getSegments().size(); i++) {
            if (getSegment(i).equals(segment)) {
                return i;
            }
        }

        return null;
    }

    /**
     * Clears the same field from each occurrences of a segment.
     * 
     * @param segment
     * @param fieldIndex
     */
    public void clearField(String segmentName, int fieldIndex) throws Exception {
        for (Segment segment : getSegments(segmentName)) {
            segment.clearField(fieldIndex);
        }
    }

    /**
     * Clears the same sub field from each occurrence of a segment.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @throws Exception
     */
    public void clearSubField(String segmentName, int fieldIndex, int subFieldIndex) throws Exception {
        for (Segment segment : getSegments(segmentName)) {
            segment.clearSubField(fieldIndex, subFieldIndex);
        }
    }

    /**
     * Sets a field to the same value in all occurrences of a segment.
     * 
     * @param segment
     * @param fieldIndex
     */
    public void setField(String segmentName, int fieldIndex, String value) throws Exception {
        for (Segment segment : getSegments(segmentName)) {
            Field field = segment.getField(fieldIndex);

            if (field != null) {
                for (FieldRepetition repetition : field.getRepetitions()) {
                    repetition.setValue(value);
                }
            }
        }
    }

    /**
     * Sets a sub field to the same value in all occurrences of a segment.
     * 
     * @param segment
     * @param fieldIndex
     * @param subFieldIndex
     */
    public void setSubField(String segmentName, int fieldIndex, int subFieldIndex, String value) throws Exception {
        for (Segment segment : getSegments(segmentName)) {
            segment.setSubField(fieldIndex, subFieldIndex, value);
        }
    }

    /**
     * Clears all fields from a segment starting at the supplied startingFieldIndex
     * in all matching segments.
     * 
     * @param segment
     * @param startingFieldIndex
     */
    public void clearFieldsStartingFrom(String segmentName, int startingFieldIndex) throws Exception {
        clearFieldRange(segmentName, startingFieldIndex, -1);
    }

    /**
     * Clears all fields from the supplied startingFieldIndex to the
     * endingFieldIndex in all matching segments.
     * 
     * @param segment
     * @param startingFieldIndex
     * @param endingFieldIndex
     */
    public void clearFieldRange(String segmentName, int startingFieldIndex, int endingFieldIndex) throws Exception {

        for (Segment segment : getSegments(segmentName)) {
            segment.clearFieldRange(startingFieldIndex, endingFieldIndex);
        }
    }

    /**
     * Clears all sub fields from a segment starting at the supplied
     * startingSubFieldIndex in all matching segments.
     * 
     * @param segment
     * @param fieldIndex            The field to clear the sub fields from
     * @param startingSubFieldIndex
     */
    public void clearSubFieldsStartingFrom(String segmentName, int fieldIndex, int startingSubFieldIndex) throws Exception {
        clearSubFieldRange(segmentName, fieldIndex, startingSubFieldIndex, -1);
    }

    /**
     * Clears all sub fields from the supplied startingSubFieldIndex to the
     * endingSubFieldIndex in all matching segments.
     * 
     * @param segment
     * @param startingFieldIndex
     * @param endingFieldIndex
     */
    public void clearSubFieldRange(String segmentName, int fieldIndex, int startingSubFieldIndex, int endingSubFieldIndex)
            throws Exception {

        for (Segment segment : getSegments(segmentName)) {
            segment.clearSubFieldRange(fieldIndex, startingSubFieldIndex, endingSubFieldIndex);
        }
    }

    /**
     * Returns the number of segment groups for the supplied segment.
     * 
     * @param segmentName
     * @return
     */
    public Integer getNumberOfSegmentGroups(String segmentName) throws Exception {
        return getStartIndexesOfSegmentGroups(segmentName).size();
    }

    /**
     * Returns the start index of the segment group.
     * 
     * @param segmentName
     * @param groupNumber
     * @return
     */
    public Integer getStartIndexOfSegmentGroup(String segmentName, int groupNumber) throws Exception {
        List<Integer> groupStartIndexes = getStartIndexesOfSegmentGroups(segmentName);

        if (groupStartIndexes.size() >= groupNumber) {
            return groupStartIndexes.get(groupNumber);
        }

        return null;
    }

    /**
     * Returns the end index of the segment group.
     * 
     * @param segmentName
     * @param groupNumber
     * @return
     */
    public Integer getEndIndexOfSegmentGroup(String segmentName, int groupNumber) throws Exception {
        List<Integer> groupStartIndexes = getStartIndexesOfSegmentGroups(segmentName);

        if (groupStartIndexes.size() >= groupNumber) {
            int startIndex = groupStartIndexes.get(groupNumber);

            for (int i = startIndex; i < this.getSegments().size(); i++) {
                Segment segment = this.getSegment(i);

                if (!segment.getName().equalsIgnoreCase(segmentName)) {
                    return --i; // The group ended at the index before this one.
                }
            }
        }

        return this.segments.size() - 1; // The segment ended with the last element in the list.
    }

    /**
     * Returns all the segments within a group
     * 
     * @param segmentName
     * @param groupNumber
     * @return
     */
    public List<Segment> getSegmentsWithinGroup(String segmentName, int groupNumber) throws Exception {
        List<Segment> segmentsWithingroup = new ArrayList<>();

        Integer startIndex = getStartIndexOfSegmentGroup(segmentName, groupNumber);

        if (startIndex == null) {
            return segmentsWithingroup;
        }

        Integer endIndex = getEndIndexOfSegmentGroup(segmentName, groupNumber);

        if (endIndex == null) {
            return segmentsWithingroup;
        }

        for (int i = startIndex; i <= endIndex; i++) {
            Segment segment = this.getSegment(i);
            segmentsWithingroup.add(segment);
        }

        return segmentsWithingroup;
    }

    /**
     * Returns an array of indexes which are the start indexes of each segment
     * group.
     * 
     * @param segmentName
     * @return
     */
    public List<Integer> getStartIndexesOfSegmentGroups(String segmentName) throws Exception {
        List<Integer> startGroupIndexes = new ArrayList<>();

        boolean groupFound = false;

        for (int i = 0; i < this.getSegments().size(); i++) {
            Segment segment = this.getSegment(i);

            if (segmentName.equals(segment.getName())) {
                if (!groupFound) {
                    groupFound = true;
                    startGroupIndexes.add(i);
                }
            } else {
                groupFound = false;
            }
        }

        return startGroupIndexes;
    }

    /**
     * Appends a new segment to a group.
     * 
     * @param segmentName
     * @param groupNumber
     * @return
     * @throws Exception
     */
    public Segment appendSegmentToGroup(String segmentName, int groupNumber) throws Exception {
        if (groupNumber >= getNumberOfSegmentGroups(segmentName)) {
            return null; // Just ignore
        }

        int endIndexOfgroup = getEndIndexOfSegmentGroup(segmentName, groupNumber);

        Segment endGroupSegment = getSegment(endIndexOfgroup);
        int currentId = Integer.valueOf(endGroupSegment.getField(1).value()).intValue();

        return insertSegment(segmentName, ++endIndexOfgroup, ++currentId);
    }

    /**
     * Does the supplied value appear in the specified field of any matching
     * segment. All field repetitions are checked.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param value
     * @return
     */
    public boolean hasFieldMatchingValue(String segmentName, int fieldIndex, String... matchValues) throws Exception {
        return hasFieldMatchingValue("equals", segmentName, fieldIndex, matchValues);
    }

    /**
     * * Does the supplied value appear in the specified field of any matching
     * segment. All field repetitions are checked.
     * 
     * @param matchType   The type of match to perform.
     * @param segmentName
     * @param fieldIndex
     * @param value
     * @return
     * @throws Exception
     */
    public boolean hasFieldMatchingValue(String matchType, String segmentName, int fieldIndex, String... matchValues)
            throws Exception {
        for (Segment segment : this.getSegments(segmentName)) {
            if (segment.hasFieldMatchingValue(matchType, fieldIndex, matchValues)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Does the supplied value appear in the specified subField of any matching
     * segment. All field repetitions are checked.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public boolean hasSubFieldMatchingValue(String segmentName, int fieldIndex, int subFieldIndex, String... matchValues)
            throws Exception {
        return hasSubFieldMatchingValue("equals", segmentName, fieldIndex, subFieldIndex, matchValues);
    }

    /**
     * Does the supplied value appear in the specified subField of any matching
     * segment. All field repetitions are checked.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public boolean hasSubFieldMatchingValue(String matchType, String segmentName, int fieldIndex, int subFieldIndex,
            String... matchValues) throws Exception {
        for (Segment segment : this.getSegments(segmentName)) {
            if (segment.hasSubFieldMatchingValue(matchType, fieldIndex, subFieldIndex, matchValues)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets a Segment Matching the specified value. All matching segments are
     * searched. All field repetitions are searched. The first match is returned.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public Segment getSegmentMatchingValue(String segmentName, int fieldIndex, int subFieldIndex, String... matchValues)
            throws Exception {
        return getSegmentMatchingValue("equals", segmentName, fieldIndex, subFieldIndex, matchValues);
    }

    /**
     * Gets a Segment Matching the specified value. All matching segments are
     * searched. All field repetitions are searched. The first match is returned.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public Segment getSegmentMatchingValue(String matchType, String segmentName, int fieldIndex, int subFieldIndex,
            String... matchValues) throws Exception {
        for (Segment segment : this.getSegments(segmentName)) {
            if (segment.hasSubFieldMatchingValue(matchType, fieldIndex, subFieldIndex, matchValues)) {
                return segment;
            }
        }

        return null;
    }

    /**
     * Gets a list of segments Matching the specified value. All matching segments
     * are searched. All field repetitions are searched
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public List<Segment> getSegmentsMatchingValue(String segmentName, int fieldIndex, int subFieldIndex, String... matchValues)
            throws Exception {
        return getSegmentsMatchingValue("equals", segmentName, fieldIndex, subFieldIndex, matchValues);
    }

    /**
     * Gets a list of segments Matching the specified value. All matching segments
     * are searched. All field repetitions are searched
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public List<Segment> getSegmentsMatchingValue(String matchType, String segmentName, int fieldIndex, int subFieldIndex,
            String... matchValues) throws Exception {
        List<Segment> segments = new ArrayList<>();

        for (Segment segment : this.getSegments(segmentName)) {
            if (segment.hasSubFieldMatchingValue(matchType, fieldIndex, subFieldIndex, matchValues)) {
                segments.add(segment);
            }
        }

        return segments;
    }

    /**
     * Gets a Segment Matching the specified value. All matching segments are
     * searched. All field repetitions are searched. The first match is returned.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public Segment getSegmentMatchingValue(String segmentName, int fieldIndex, String... matchValues) throws Exception {
        return getSegmentMatchingValue("equals", segmentName, fieldIndex, matchValues);
    }

    /**
     * Gets a Segment Matching the specified value. All matching segments are
     * searched. All field repetitions are searched. The first match is returned.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public Segment getSegmentMatchingValue(String matchType, String segmentName, int fieldIndex, String... matchValues)
            throws Exception {
        for (Segment segment : this.getSegments(segmentName)) {
            if (segment.hasFieldMatchingValue(matchType, fieldIndex, matchValues)) {
                return segment;
            }
        }

        return null;
    }

    /**
     * Gets a list of segments Matching the specified value. All matching segments
     * are searched. All field repetitions are searched.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public List<Segment> getSegmentsMatchingValue(String segmentName, int fieldIndex, String... matchValues) throws Exception {
        return getSegmentsMatchingValue("equals", segmentName, fieldIndex, matchValues);
    }

    /**
     * Gets a list of segments Matching the specified value. All matching segments
     * are searched. All field repetitions are searched.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     * @return
     */
    public List<Segment> getSegmentsMatchingValue(String matchType, String segmentName, int fieldIndex, String... matchValues)
            throws Exception {
        List<Segment> segments = new ArrayList<>();

        for (Segment segment : this.getSegments(segmentName)) {
            if (segment.hasFieldMatchingValue(matchType, fieldIndex, matchValues)) {
                segments.add(segment);
            }
        }

        return segments;
    }

    /**
     * Removes all matching segments.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param value
     */
    public void removeSegmentsMatchingValue(String segmentName, int fieldIndex, String... matchValues) throws Exception {
        removeSegmentsMatchingValue("equals", segmentName, fieldIndex, matchValues);
    }

    /**
     * Removes all matching segments.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param value
     */
    public void removeSegmentsMatchingValue(String matchType, String segmentName, int fieldIndex, String... matchValues)
            throws Exception {
        List<Segment> segments = this.getSegmentsMatchingValue(matchType, segmentName, fieldIndex, matchValues);
        removeSegments(segments);
    }

    /**
     * Removes all matching segments.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     */
    public void removeSegmentsMatchingValue(String segmentName, int fieldIndex, int subFieldIndex, String... matchValues)
            throws Exception {
        removeSegmentsMatchingValue("equals", segmentName, fieldIndex, subFieldIndex, matchValues);
    }

    /**
     * Removes all matching segments.
     * 
     * @param segmentName
     * @param fieldIndex
     * @param subFieldIndex
     * @param value
     */
    public void removeSegmentsMatchingValue(String matchType, String segmentName, int fieldIndex, int subFieldIndex,
            String... matchValues) throws Exception {
        List<Segment> segments = this.getSegmentsMatchingValue(matchType, segmentName, fieldIndex, subFieldIndex, matchValues);
        removeSegments(segments);
    }

    /**
     * Removes a field repetition where the matchValue matches the subField value.
     * 
     * @param fieldIndex
     * @param subFieldIndex
     * @param matchValue
     * @return
     */
    public void removeMatchingFieldRepetitions(String segmentName, int fieldIndex, int subFieldIndex, String... matchValues)
            throws Exception {
        removeMatchingFieldRepetitions("equals", segmentName, fieldIndex, subFieldIndex, matchValues);
    }

    /**
     * Removes a field repetitions.
     * 
     * @param fieldIndex
     * @param subFieldIndex
     * @param matchValue
     * @return
     */
    public void removeMatchingFieldRepetitions(String matchType, String segmentName, int fieldIndex, int subFieldIndex,
            String... matchValues) throws Exception {
        for (Segment segment : this.getSegments(segmentName)) {
            segment.removeMatchingFieldRepetitions(matchType, fieldIndex, subFieldIndex, matchValues);
        }
    }

    /**
     * Returns a segment.
     * 
     * @param segmentName
     * @param occurrence
     * @return
     * @throws Exception
     */
    public Segment getSegment(String segmentName, int occurrence) throws Exception {
        Integer index = getSegmentIndex(segmentName, occurrence);

        // If the index is null which means the segment does not exist then create and
        // empty segment and return it to prevent NPE's. The segment is not added to the
        // message.
        if (index == null) {
            return createSegment("");
        }

        return getSegment(index);
    }

    /**
     * Returns the 1st occurrence of a segment.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public Segment getSegment(String segmentName) throws Exception {
        return getSegment(segmentName, 0);
    }

    /**
     * Returns the message row index of the last occurrence of the supplied segment
     * name.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public Integer getLastSegmentIndex(String segmentName) throws Exception {
        List<Integer> segmentIndexes = getSegmentIndexes(segmentName);

        if (segmentIndexes.isEmpty()) {
            return null;
        }

        return segmentIndexes.get(segmentIndexes.size() - 1);
    }

    /**
     * Is the message of the supplied type.
     * 
     * @param messageType
     * @return
     * @throws Exception
     */
    public boolean isType(String messageType) throws Exception {
        Field messageTypeField = getMessageTypeField();

        String type = null;

        if (messageTypeField != null) {
            type = messageTypeField.getSubField(1).value() + "_" + messageTypeField.getSubFieldValue(2);
        }

        if (messageType.endsWith("_*")) {
            return type.substring(0, 3).equals(messageType.substring(0, 3));
        }

        return type.equals(messageType);
    }

    /**
     * Changes the version of this message.
     * 
     * @param newVersion
     * @throws Exception
     */
    public void changeMessageVersion(String newVersion) throws Exception {
        getMSHSegment().changeMessageVersion(newVersion);
    }

    /**
     * Removes a patient identifier from the PID segment.
     * 
     * @param message
     * @param identifier
     * @throws Exception
     */
    public void removePatientIdentifierField(String identifier) throws Exception {
        getPIDSegment().removePatientIdentifierField(identifier);
    }

    /**
     * Gets a patient identifier value from the PID segment
     * 
     * @param identifier
     * @return
     * @throws Exception
     */
    public String getPatientIdentifierValue(String identifier) throws Exception {
        return getPIDSegment().getPatientIdentifierValue(identifier);
    }

    /**
     * Returns a list of patient identifiers in the PID segment.
     * 
     * @return
     * @throws Exception
     */
    public List<String> getPatientIdentifierCodes() throws Exception {
        return getPIDSegment().getPatientIdentifierCodes();
    }

    /**
     * Removes patient identifiers which do not match the identifier to keep.
     * 
     * @param identifierToKeep
     * @throws Exception
     */
    public void removeOtherPatientIdentifierFields(String identifierToKeep) throws Exception {
        getPIDSegment().removeOtherPatientIdentifierFields(identifierToKeep);
    }

    /**
     * Sets the segments which must not be removed from this message.
     * 
     * @param segmentsToKeep
     * @throws Exception
     */
    public void setSegmentsToKeep(String... segmentsToKeep) throws Exception {

        List<String> segmentsToRemove = new ArrayList<>();

        for (Segment segment : getSegments()) {
            if (!doesContainSegment(segment.getName(), segmentsToKeep)) {
                segmentsToRemove.add(segment.getName());
            }
        }

        for (String segmentToRemove : segmentsToRemove) {
            removeAllSegments(segmentToRemove);
        }
    }

    /**
     * 
     * @param segmentName
     * @param requiredSegments
     * @return
     */
    private boolean doesContainSegment(String segmentName, String[] requiredSegments) {
        for (String requiredSegment : requiredSegments) {
            if (segmentName.equals(requiredSegment)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all segments matching the segment name no matter where they appear in
     * the message.
     * 
     * @param segmentName
     * @throws Exception
     */
    public void removeAllSegments(String segmentName) throws Exception {
        for (Iterator<Segment> iter = getSegments().iterator(); iter.hasNext();) {
            Segment segment = iter.next();

            if (segment.getName().equals(segmentName)) {
                iter.remove();
            }
        }
    }

    /**
     * Returns the total number of segments in this message.
     * 
     * @return
     */
    public int getTotalSegmentCount() {
        return getSegments().size();
    }

    private Segment createSegment(String value) {
        Segment segment = null;

        if (value.startsWith("MSH")) {
            segment = new MSHSegment(value, this);
        } else if (value.startsWith("PID")) {
            segment = new PIDSegment(value, this);
        } else {
            segment = new Segment(value, this);
        }

        return segment;
    }

    /**
     * Returns an empty segment. The segment is not added to the message.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public Segment createEmptySegment(String segmentName) throws Exception {
        return new Segment(segmentName, this);
    }

    /**
     * Returns an empty field repetition. The field repetition is not added to the
     * message.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public FieldRepetition createEmptyFieldRepetition() throws Exception {
        return new FieldRepetition();
    }

    /**
     * Returns an empty field. The field is not added to the message.
     * 
     * @param segmentName
     * @return
     * @throws Exception
     */
    public Field createEmptyField() throws Exception {
        return new Field();
    }
}
