import java.util.LinkedList;

public class PriorityQueue314<E extends Comparable<? super E>> {
    LinkedList<E> list; 

    public PriorityQueue314(){
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
    }
}
