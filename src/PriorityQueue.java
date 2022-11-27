<<<<<<< HEAD

import java.util.ArrayList;
import java.util.List; 
import java.util.LinkedList;

public class PriorityQueue<E extends Comparable<? super E>> {
    LinkedList<E> list; 

    public PriorityQueue(){
        list = new LinkedList<>();
    }

    public boolean enque(E object){
        if(list.size() == 0){
            list.addFirst(object);
            return true; 
        }
        for(int x = 0; x < list.size(); x++){
            if(object.compareTo(list.get(x)) < 0){
                list.add(x, object);
                return true; 
            }
        }
        list.addLast(object);
        return true; 
    }
    
    public E deque(){
        E obj = list.getFirst();
        list.removeFirst();
        return obj; 
    }

    public int size(){
        return list.size();
    }

    @Override
    public String toString() {
        StringBuilder x = new StringBuilder();
        for(E item: list){
            x.append(item.toString() + "\n");
        }

        return x.toString();
=======
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
>>>>>>> 5a6c661c2b0a87af3472e4f31c6f2c6a70b6dcfd
    }
}
