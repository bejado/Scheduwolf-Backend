package Coursewiz;

import java.util.List;
import java.util.ArrayList;
import Coursewiz.Choice;

public class ChoiceNode<T extends Choice> {
	ChoiceNode<T> next = null;
	
	private List<T> choices = new ArrayList<T>();
	private List< List<T> > generatedChoices = null;
	private int amount = 1;
	private boolean useGenerated = false;
	
	/**
	 * Set the amount of choices to be chosen from this node. Does not automatically calculate choices.
	 * Recalculate via calculateChoices()
	 */
	public void setAmount(int a) {
		if (a >= 1) {
			amount = a;
		}  else if (amount == 1) {
			useGenerated = false;
		}
	}
	
	public int getAmount() {
		return amount;
	}
	
	/**
	 * Add a choice to this node. Does not automatically calculate choices.
	 * Recalculate via calculateChoices()
	 */
	public void addChoice(T choice) {
		choices.add(choice);
	}
	
	/**
	 * Add multiple choices to this node. Does not automatically calculate choices.
	 * Recalculate via calculateChoices()
	 */
	public void addChoices(List<T> newChoices) {
		choices.addAll(newChoices);
	}
	
	private void calculateChoices() {
		if (amount > 1) {
			generatedChoices = generateCombinations(choices, amount);
			useGenerated = true;
		}
	}
	
    public List< List<T> > generateChoices() {
    	List< List<T> > combinations = new ArrayList< List<T> >();
    	this.node(0, new ArrayList<T>(), combinations);
    	return combinations;
    }
	
    private void node(int depth, List<T> tmp, List< List<T> > combinations) {
    	
    	if (!useGenerated) {
    	
	    	for (T thisItem : choices) {
	    		
	    		// Check to see if our new addition works with the previous ones
    			Boolean works = true;
    			for (int i = 0; i < depth; i++) {
    				T item = tmp.get(i);
    				if (!item.worksWith(thisItem)) {
    					works = false;
    					break;
    				}
    			}
    			if (!works) continue;
	    		
    			// Add the item to tmp
    			// We either replace a previous entry, or add a new one
	    		if (tmp.size() > depth) {
	    			tmp.set(depth, thisItem);
	    		} else {
	    			tmp.add(thisItem);
	    		}
	    		
	    		if (this.next == null) {
	    			combinations.add(new ArrayList<T>(tmp));
	    		} else {
	    			this.next.node(depth + 1, tmp, combinations);
	    		}
	    	}
    	
    	} else {
    		
	    	for (List<T> thisList : generatedChoices) {
	    		int offset = 0;
	    		
	    		// Check to see if every new addition works with the previous ones
    			Boolean works = true;
    			for (int i = 0; i < depth; i++) {
    				T item = tmp.get(i);
    				for (T thisItem : thisList) {
	    				if (!item.worksWith(thisItem)) {
	    					works = false;
	    					break;
	    				}
    				}
    				if (!works) break;
    			}
    			if (!works) continue;
	    		
	    		for (T thisItem : thisList) {
		    		if (tmp.size() > depth + offset) {
		    			tmp.set(depth + offset, thisItem);
		    		} else {
		    			tmp.add(thisItem);
		    		}
		    		offset++;
	    		}
	    		
	    		if (this.next == null) {
	    			combinations.add(new ArrayList<T>(tmp));
	    		} else {
	    			this.next.node(depth + offset, tmp, combinations);
	    		}
	    	}
	    	
    	}
    }

    private static <T> List< List<T> > generateCombinations(List<T> array, int amount) {
    	List<Integer> tmp = new ArrayList<Integer>();
        for (int i = 0; i < amount; i ++) {
        	tmp.add(-1);
        }
        
        List< List<T> > combinations = new ArrayList< List<T> >();
        combine(array, 0, 0, tmp, combinations);
        return combinations;
    }
    
    private static <T> void combine(List<T> array, int start, int depth, List<Integer> tmp, List< List<T> > combinations) {
    	 
        if (depth == tmp.size()) {
            List<T> newCombination = new ArrayList<T>();
            for (int j = 0; j < depth; j++) {
                newCombination.add(array.get(tmp.get(j)));
            }
            combinations.add(newCombination);
            return;
        }
        for (int i = start; i < array.size(); i++) {
        	tmp.set(depth, i);
            combine(array, i + 1, depth + 1, tmp, combinations);
        }
 
    }
}
