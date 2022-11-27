import java.util.Iterator;
import java.util.LinkedList;

public class PriorityQueue<E extends Comparable> {
    private LinkedList<E> queueList;

    public PriorityQueue () {
        queueList = new LinkedList<>();
    }

    public void enqueue(E e) {
        if(queueList.getFirst().compareTo(e) > 0) {
            queueList.addFirst(e);
        }
        else if (queueList.getLast().compareTo(e) < 0) {
            queueList.addLast(e);
        }
        else {
            Iterator tempIter = queueList.iterator();
            int index = 0;
            boolean found = false;
            while(tempIter.hasNext() && !found) {
                E tempE = (E)tempIter.next();
                if(tempE.compareTo(e) < 0) {
                    found = true;
                    queueList.add(index, e);
                }
                index++;
            }
        }
    }

    public void dequeue() {
        queueList.removeFirst();
    }
}
