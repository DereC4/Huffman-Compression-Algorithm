/*  Student information for assignment:
 *
 *  On <MY|OUR> honor, <NAME1> and <NAME2), this programming assignment is <MY|OUR> own work
 *  and <I|WE> have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID:
 *  email address:
 *  Grader name:
 *
 *  Student 2
 *  UTEID:
 *  email address:
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private Map<Integer, Integer> mapofwords;
    private PriorityQueue314<TreeNode> queue;
    private Map<Integer, String> values;
    private TreeNode root;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     *
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     *                     header to use, standard count format, standard tree format, or
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
        mapofwords = getMapOfFreq(bits);
        queue = getQueue(mapofwords);
        root = createTree(queue);
        values = getHuffCodes(root);

        System.out.println(mapofwords);
        System.out.println(queue);
        System.out.println(values);
        return 0;
    }

    public TreeNode createTree(PriorityQueue314<TreeNode> queue) {
        while (queue.size() > 1) {
            TreeNode left = queue.deque();
            TreeNode right = queue.deque();
            TreeNode temp = new TreeNode(left, -1, right);
            queue.enque(temp);
        }
        return queue.deque();
    }

    public Map<Integer, String> getHuffCodes(TreeNode node) {
        Map<Integer, String> curr = new TreeMap<>();
        getHuffCodesHelper(curr, "", node);
        return curr;
    }

    public void getHuffCodesHelper(Map<Integer, String> map, String current, TreeNode node) {
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

    public Map<Integer, Integer> getMapOfFreq(BitInputStream bits) throws IOException {
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

    public PriorityQueue314<TreeNode> getQueue(Map<Integer, Integer> mapofwords) {
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
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     *
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     *              If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        BitInputStream bits = new BitInputStream(in);
        BitOutputStream outs = new BitOutputStream(out);
        outs.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        outs.writeBits(BITS_PER_INT, STORE_COUNTS);
        System.out.println(mapofwords);


        for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
            if (mapofwords.containsKey(k)) {
                outs.writeBits(BITS_PER_INT, mapofwords.get(k));
            } else {
                outs.writeBits(BITS_PER_INT, 0);
            }
        }
        //consider writing for 256


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

        return 0;
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
        if(countType == STORE_COUNTS) {
            Map<Integer, Integer> tempFreq = new TreeMap<>();
            // Undo the SCF compression which stored every character frequency
            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                int frequencyInOriginalFile = bis.readBits(BITS_PER_INT);
                if (frequencyInOriginalFile > 0) {
                    tempFreq.put(k, frequencyInOriginalFile);
                }
            }
            tempFreq.put(IHuffConstants.PSEUDO_EOF, 1);
            PriorityQueue314<TreeNode> decompQueue = getQueue(tempFreq);
            TreeNode decompRoot = createTree(decompQueue);
            int temp = decoder(bis, bos, decompRoot);
        }
        else if (countType == STORE_TREE) {
            TreeNode decompRoot = new TreeNode(-1 , -1);
            int bitNum = bis.readBits(BITS_PER_INT);
            decompRoot = createTreeRec(bis, decompRoot);
            int temp = decoder(bis, bos, decompRoot);
        }
        return 0;
    }

    private int decoder(BitInputStream bis, BitOutputStream bos, TreeNode decompRoot) throws IOException {
        boolean finished = false;
        TreeNode current = decompRoot;
        int dirCheck = 0;
        while (!finished && !(dirCheck == -1)) {
            dirCheck = bis.readBits(1);
            if (dirCheck == -1) {
                System.out.println("NO EOF");
//                throw new IOException("Error reading compressed file. \n" +
//                        "unexpected end of input. No PSEUDO_EOF value.");
            } else {
                if (dirCheck == 0) {
                    current = current.getLeft();
                } else if (dirCheck == 1) {
                    current = current.getRight();
                }
                if (current.getValue() == PSEUDO_EOF) {
                    System.out.println("FINISHED");
                    finished = true;
                } else if (current.isLeaf()) {
                    int treeVal = current.getValue();
                    System.out.println(treeVal);
                    bos.writeBits(BITS_PER_INT, treeVal);
                    current = decompRoot;
                }
            }
        }
        return 0;
    }

    public TreeNode createTreeRec(BitInputStream bis, TreeNode current) throws IOException {
        int tempBit = bis.readBits(1);
        if(tempBit == 0) {
            // Internal node
            TreeNode tempNode = new TreeNode(-1, -1);
            tempNode.setLeft(createTreeRec(bis, tempNode));
            tempNode.setRight(createTreeRec(bis, tempNode));
            return tempNode;
        }
        else if(tempBit == 1) {
            // Leaf node
            int nodeValue = bis.readBits(9);
            TreeNode tempNode = new TreeNode(nodeValue, -1);
            return tempNode;
        }
        return null;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
