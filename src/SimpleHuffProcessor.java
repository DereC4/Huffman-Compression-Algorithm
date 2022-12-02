/*  Student information for assignment:
 *
 *  On OUR honor, Derek and Shanti, this programming assignment is <MY|OUR> own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1
 *  UTEID: dyc377
 *  email address: derexh2so4@utexas.edu
 *  Grader name: Lilly Tian
 *
 *  Student 2
 *  UTEID: sc66349
 *  email address: shantikiranc@utexas.edu
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private Map<Integer, Integer> mapofwords;
    private PriorityQueue314<TreeNode> queue;
    private Map<Integer, String> values;
    private TreeNode root;
    private int header;
    private int sizeOfNewFile;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     *
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind
     *                     of
     *                     header to use, standard count format, standard tree
     *                     format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        BitInputStream bits = new BitInputStream(in);
        showString("Opening file...");
        int size = 0;
        while (bits.readBits(1) != -1) {
            size++;
        }
        in.reset();
        bits = new BitInputStream(in);
        // Process for getting word frequencies, enqueueing them, and converting to Huffman Tree
        mapofwords = getMapOfFreq(bits);
        showString("Map of frequencies created! " + mapofwords);
        queue = getQueue(mapofwords);
        root = createTree(queue);
        header = headerFormat;
        Map<Integer, String> curr = new TreeMap<>();
        getHuffCodesHelper(curr, "", root);
        values = curr;
        in.reset();
        bits = new BitInputStream(in);
        if (header == IHuffConstants.STORE_COUNTS) {
            sizeOfNewFile = getSizeOfFileCounts(bits);
        } else {
            sizeOfNewFile = getSizeOfFileTree(bits);
        }
        int saved = size - sizeOfNewFile;
        showString("Saved: " + saved + " bits");
        return saved;
    }

    /**
     * Helper method to get the size of file in SCF format
     *
     * @return The size of file in SCF format
     * @throws IOException
     */
    private int getSizeOfFileCounts(BitInputStream bits) throws IOException {
        int total = 0;
        total += BITS_PER_INT * 2;
        total += BITS_PER_INT * ALPH_SIZE;
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        while (inbits != -1) {
            String value = values.get(inbits);
            total += value.length();
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        }
        String value = values.get(PSEUDO_EOF);
        total += value.length();
        return total;
    }

    /**
     * Helper method to get the size of file in STF format
     *
     * @return The size of file in STF format
     * @throws IOException
     */
    private int getSizeOfFileTree(BitInputStream bits) throws IOException {
        int total = 0;
        total += BITS_PER_INT * 3;
        TreeNode n = root;
        int allNodes = getSize(n);
        int size = mapofwords.size() * 9 + allNodes;
        total += size;
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        while (inbits != -1) {
            String value = values.get(inbits);
            total += value.length();
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        }
        String value = values.get(PSEUDO_EOF);
        total += value.length();
        return total;
    }

    /**
     * Creates the Huffman Tree given a PriorityQueue314 with frequencies
     *
     * @return The last value in the queue, the root of the Huffman Tree
     */
    private TreeNode createTree(PriorityQueue314<TreeNode> queue) {
        while (queue.size() > 1) {
            TreeNode left = queue.deque();
            TreeNode right = queue.deque();
            TreeNode temp = new TreeNode(left, -1, right);
            queue.enque(temp);
        }
        showString("Huffman Tree Created!");
        return queue.deque();
    }

    /**
     * Creates a map of the binary values representing the characters in the Huffman Tree by
     * traversing down the tree
     */
    private void getHuffCodesHelper(Map<Integer, String> map, String current, TreeNode node) {
        if (node.getLeft() != null && node.getRight() != null) {
            String leftCurr = current + "0";
            String rightCurr = current + "1";
            getHuffCodesHelper(map, leftCurr, node.getLeft());
            getHuffCodesHelper(map, rightCurr, node.getRight());
        } else if (node.getLeft() != null) {
            String curr = current + "0";
            getHuffCodesHelper(map, current, node.getLeft());
        } else if (node.getRight() != null) {
            String curr = current + "1";
            getHuffCodesHelper(map, current, node.getRight());
        } else {
            map.put(node.getValue(), current);
        }
    }

    /**
     * Generates the map of frequencies for the characters based on the input stream
     *
     * @throws IOException
     */
    private Map<Integer, Integer> getMapOfFreq(BitInputStream bits) throws IOException {
        Map<Integer, Integer> mapofwords = new TreeMap<>();
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        while (inbits != -1) {
            if (mapofwords.containsKey(inbits)) {
                int x = mapofwords.get(inbits);
                mapofwords.put(inbits, x + 1);
            } else {
                mapofwords.put(inbits, 1);
            }
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        }
        mapofwords.put(IHuffConstants.PSEUDO_EOF, 1);

        return mapofwords;
    }

    /**
     * Generates the PriorityQueue314 based on the frequency map provided
     */
    private PriorityQueue314<TreeNode> getQueue(Map<Integer, Integer> mapofwords) {
        PriorityQueue314<TreeNode> queue = new PriorityQueue314<>();
        for (Map.Entry<Integer, Integer> entry : mapofwords.entrySet()) {
            TreeNode node = new TreeNode(entry.getKey(), entry.getValue());
            queue.enque(node);
        }
        return queue;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br>
     * pre: <code>preprocessCompress</code> must be called before this method
     *
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than
     *              the input file.
     *              If this is false do not create the output file if it is larger
     *              than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        BitInputStream bits = new BitInputStream(in);
        BitOutputStream outs = new BitOutputStream(out);
        outs.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        if (header == IHuffConstants.STORE_COUNTS) {
            outs.writeBits(BITS_PER_INT, STORE_COUNTS);
            standardCountsHeader(outs);
        } else {
            outs.writeBits(BITS_PER_INT, STORE_TREE);
            standardTreeHeader(outs);
        }
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        while (inbits != -1) {
            String value = values.get(inbits);
            for (int x = 0; x < value.length(); x++) {
                outs.writeBits(1, value.charAt(x));
            }
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        }
        String value = values.get(PSEUDO_EOF);
        for (int x = 0; x < value.length(); x++) {
            outs.writeBits(1, value.charAt(x));
        }
        outs.close();
        showString("Size of new file: " + sizeOfNewFile);
        return sizeOfNewFile;
    }

    /**
     * Helper method writing the standard counts header
     */
    private void standardCountsHeader(BitOutputStream outs) {
        for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
            if (mapofwords.containsKey(k)) {
                outs.writeBits(BITS_PER_INT, mapofwords.get(k));
            } else {
                outs.writeBits(BITS_PER_INT, 0);
            }
        }
    }

    /**
     * @return The size of the Huffman Tree
     */
    private int getSize(TreeNode n) {
        if (n == null) {
            return 0;
        } else {
            return getSize(n.getLeft()) + 1 + getSize(n.getRight());
        }
    }

    /**
     * Helper method that writes the standard tree header
     * \
     */
    private void standardTreeHeader(BitOutputStream outs) {
        TreeNode n = root;
        int allNodes = getSize(n);
        int size = mapofwords.size() * 9 + allNodes;
        outs.writeBits(BITS_PER_INT, size);
        treeToBinary(n, outs);
    }

    /**
     * Helper method converting the huffman tree to binary
     */
    private void treeToBinary(TreeNode n, BitOutputStream outs) {
        if (n != null) {
            if (n.getValue() != -1) {
                outs.writeBits(1, 1);
                outs.writeBits(IHuffConstants.BITS_PER_WORD + 1, n.getValue());
            } else {
                outs.writeBits(1, 0);
                treeToBinary(n.getLeft(), outs);
                treeToBinary(n.getRight(), outs);
            }
        }
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     *
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream bis = new BitInputStream(in);
        BitOutputStream bos = new BitOutputStream(out);
        int magicNumba = bis.readBits(BITS_PER_INT);
        if (magicNumba != MAGIC_NUMBER) {
            myViewer.showError("Error reading compressed file. \n" +
                    "File did not start with the huff magic number.");
        }
        int countType = bis.readBits(BITS_PER_INT);
        int bitCount = 0;
        TreeNode decompRoot;
        // Check the type of encoding (STF) (SCF)
        if (countType == STORE_COUNTS) {
            showString("Uncompressing in SCF");
            decompRoot = createTreeSCF(bis);
            bitCount = decoder(bis, bos, decompRoot);
        } else if (countType == STORE_TREE) {
            showString("Uncompressing in STF");
            int numBits = bis.readBits(BITS_PER_INT);
            decompRoot = createTreeFromBits(bis);
            showString("Huffman Tree Created!");
            bitCount = decoder(bis, bos, decompRoot);
        }
        showString(bitCount + " bits written.");
        bos.close();
        return bitCount;
    }

    /**
     * Reads in bit by bit and traverses the huffman tree depending on the bit value
     * A bit value of 0 corresponds to a left traversal while 1 represents a right
     * traversal
     *
     * @throws IOException
     */
    private int decoder(BitInputStream bis, BitOutputStream bos, TreeNode root) throws IOException {
        boolean finished = false;
        TreeNode current = root;
        int bitCount = 0;
        int dirCheck = 0;
        while (!finished && !(dirCheck == -1)) {
            dirCheck = bis.readBits(1);
            if (dirCheck == -1) {
                throw new IOException("Error reading compressed file. \n" +
                        "unexpected end of input. No PSEUDO_EOF value.");
            } else {
                if (dirCheck == 0) {
                    current = current.getLeft();
                } else if (dirCheck == 1) {
                    current = current.getRight();
                }
                if (current.getValue() == PSEUDO_EOF) {
                    finished = true;
                } else if (current.isLeaf()) {
                    bos.write(current.getValue());
                    current = root;
                    bitCount++;
                }
            }
        }
        return bitCount;
    }

    /**
     * Creates a Huffman tree from a SCF header format where the frequencies and
     * values of characters in ASCII are given
     *
     * @return A TreeNode to be set as the child of a previous TreeNode
     * @throws IOException
     */
    private TreeNode createTreeSCF(BitInputStream bis) throws IOException {
        Map<Integer, Integer> tempFreq = new TreeMap<>();
        for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
            int frequencyInOriginalFile = bis.readBits(BITS_PER_INT);
            if (frequencyInOriginalFile > 0) {
                tempFreq.put(k, frequencyInOriginalFile);
            }
        }
        tempFreq.put(IHuffConstants.PSEUDO_EOF, 1);
        PriorityQueue314<TreeNode> decompQueue = getQueue(tempFreq);
        return createTree(decompQueue);
    }

    /**
     * Creates a Huffman tree from a STF header format where 0 corresponds to an
     * internal node while 1 corresponds to a leaf node
     *
     * @return A TreeNode to be set as the child of a previous TreeNode
     * @throws IOException
     */
    private TreeNode createTreeFromBits(BitInputStream bis) throws IOException {
        int tempBit = bis.readBits(1);
        if (tempBit == 0) {
            // Internal node
            TreeNode tempNode = new TreeNode(-1, -1);
            tempNode.setLeft(createTreeFromBits(bis));
            tempNode.setRight(createTreeFromBits(bis));
            return tempNode;
        } else if (tempBit == 1) {
            // Leaf node
            int nodeValue = bis.readBits(BITS_PER_WORD + 1);
            TreeNode tempNode = new TreeNode(nodeValue, -1);
            return tempNode;
        } else {
            myViewer.showError("catastrophic failure");
        }
        return null;
    }

    /**
     * @param viewer is the view for communicating.
     */
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    /**
     * Shows a string to the GUI
     * @param s
     */
    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
