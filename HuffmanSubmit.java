// Author: Linh Tran
// Contact: ltran18@u.rochester.edu

// Import any package as required
import java.io.IOException;
import java.util.*;

public class HuffmanSubmit implements Huffman {
    // Feel free to add more methods and variables as required.

    public static void main(String[] args) throws IOException {
        Huffman huffman = new HuffmanSubmit();

        //huffman.encode("lorem.txt", "lorem.enc", "lorem_freq.txt");
        //huffman.decode("lorem.enc", "lorem_dec.txt", "lorem_freq.txt");

        // After decoding, both ur.jpg and ur_dec.jpg should be the same.
        // On linux and mac, you can use `diff' command to check if they are the same.
    }

    public void encode(String inputFile, String outputFile, String freqFile) {
	// 0. Read file
        BinaryIn binIn = new BinaryIn(inputFile);

        // 1. Count the frequency
        int[] freq = new int[256];
        while (!binIn.isEmpty()) freq[(int) binIn.readChar()]++; // increase freq count

        // 2. Store characters as tree nodes and put them into a priority queue
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.getFrequency()));

        for (int i = 0; i < 256; i++) {
            if (freq[i] != 0) {
                Node node = new Node((char) i, freq[i]);
                queue.add(node);
            }
        }

        // 3. Build the tree
        createHuffmanTree(queue); // the only node left is the root of the tree

        // 4. Encode map - including an EOF character
        Map<Character, String> map = new HashMap<>();
        encodeMap(queue.peek(), "", map);

        // 5. Encode file
        BinaryIn binIn2 = new BinaryIn(inputFile);
        BinaryOut binOut = new BinaryOut(outputFile);

        while (!binIn2.isEmpty()) {
            // get code for each char
            char ch = binIn2.readChar();
            String code = map.get(ch);

            // loop through code to translate to boolean
            for (int index = 0; index < code.length(); index++) {
                if (code.charAt(index) == '0') {
                    binOut.write(false);
                }
                else if (code.charAt(index) == '1') {
                    binOut.write(true);
                }
            }
        }
        binOut.flush();

        // 6. Build frequency file
        BinaryOut frequency = new BinaryOut(freqFile);
        for (int i = 0; i < 256; i++) {
            String val = Integer.toBinaryString(i);
            String binary = "00000000".substring(val.length()) + val;
            frequency.write(binary + ":" + freq[i] + "\n");
        }
        frequency.flush();
    }

	public void decode(String inputFile, String outputFile, String freqFile){
	// 0. Make frequency queue with the frequency file
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.getFrequency()));
        BinaryIn binaryFreq = new BinaryIn(freqFile);
        String[] lines = binaryFreq.readString().split("\n");

	// Read file by line
        for (String str : lines) {
            String[] line = str.split(":");
            int freq = Integer.parseInt(line[1]); // get frequency of char
            if (freq != 0) {
                int val = Integer.parseInt(line[0], 2); // turn binary into int
                Node node = new Node((char) val, freq);
                queue.add(node);
            }
        }

        // 1. Build the tree
        createHuffmanTree(queue);

        // 2. Read the file using the tree
        BinaryIn binaryIn = new BinaryIn(inputFile);
        BinaryOut binaryOut = new BinaryOut(outputFile);

        Node current = queue.peek();

        // Travel through the file to decode
        while (!binaryIn.isEmpty()) {
            boolean code = binaryIn.readBoolean();
            if (current.isLeaf()) {
                binaryOut.write(current.getCharacter());
                current = queue.peek();
            }
            if (code && current.getRight() != null) {
                current = current.getRight();
            } else if (!code && current.getLeft() != null) {
                current = current.getLeft();
            }
        }
        binaryOut.flush();
    }

    // Node class
    private static class Node {
        private char character;
        private int frequency;
        private Node left;
        private Node right;

        public Node(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
        }
        public Node(int frequency) { this.frequency = frequency; }
        public Node() {}

        public Node getLeft() { return left; }
        public Node getRight() { return right; }
        public char getCharacter() { return character; }
        public int getFrequency() { return frequency; }

        public void setLeft(Node n) { left = n; }
        public void setRight(Node n) { right = n; }
        public void setCharacter(char character) { this.character = character; }
        public void setFrequency(int frequency) { this.frequency = frequency; }

        // Check leaf node
        private boolean isLeaf() {
            assert ((left == null) && (right == null)) || ((left != null) && (right != null));
            return (left == null) && (right == null);
        }

        // compare frequency
        public int compareTo(Node n) {
            return this.frequency - n.frequency;
        }
    }

    private static void createHuffmanTree(PriorityQueue<Node> queue) {
        while (queue.size() > 1) {
            // Get two smallest frequency char, make an internal node with them
            Node left = queue.poll();
            Node right = queue.poll();
            Node internalNode = new Node(left.getFrequency() + right.getFrequency());
            internalNode.setLeft(left);
            internalNode.setRight(right);
            // Add internal node back to the queue
            queue.add(internalNode);
        }
    }

    public static void encodeMap(Node node, String code, Map<Character, String> map) {
        if (node.isLeaf()) {
            map.put(node.getCharacter(), code);
        } else {
            if (node.getLeft() != null) { encodeMap(node.getLeft(), code + "0", map); }
            if (node.getRight() != null) { encodeMap(node.getRight(), code + "1", map); }
        }
    }

}
