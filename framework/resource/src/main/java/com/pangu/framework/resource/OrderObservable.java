package com.pangu.framework.resource;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class OrderObservable extends Observable {

	private final Vector<Observer> obs;

	

	public OrderObservable() {
		obs = new Vector<>();
	}

	/**
	 * Adds an observer to the set of observers for this object, provided that it is not the same as some observer
	 * already in the set. The order in which notifications will be delivered to multiple observers is not specified.
	 * See the class comment.
	 * @param o an observer to be added.
	 * @throws NullPointerException if the parameter o is null.
	 */
	public synchronized void addObserver(Observer o) {
		if (o == null)
			throw new NullPointerException();
		if (!obs.contains(o)) {
			obs.addElement(o);
		}
	}

	/**
	 * Deletes an observer from the set of observers of this object. Passing <CODE>null</CODE> to this method will have
	 * no effect.
	 * @param o the observer to be deleted.
	 */
	public synchronized void deleteObserver(Observer o) {
		obs.removeElement(o);
	}

	/**
	 * If this object has changed, as indicated by the <code>hasChanged</code> method, then notify all of its observers
	 * and then call the <code>clearChanged</code> method to indicate that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two arguments: this observable object and
	 * <code>null</code>. In other words, this method is equivalent to: <blockquote><tt>
	 * notifyObservers(null)</tt></blockquote>
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void notifyObservers() {
		notifyObservers(null);
	}

	/**
	 * If this object has changed, as indicated by the <code>hasChanged</code> method, then notify all of its observers
	 * and then call the <code>clearChanged</code> method to indicate that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two arguments: this observable object and the
	 * <code>arg</code> argument.
	 * @param arg any object.
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void notifyObservers(Object arg) {
		
		Object[] arrLocal;

		synchronized (this) {
			 /* We don't want the Observer doing callbacks into
             * arbitrary code while holding its own Monitor.
             * The code where we extract each Observable from
             * the Vector and store the state of the Observer
             * needs synchronization, but notifying observers
             * does not (should not).  The worst result of any
             * potential race-condition here is that:
             * 1) a newly-added Observer will miss a
             *   notification in progress
             * 2) a recently unregistered Observer will be
             *   wrongly notified when it doesn't care
             */
			if (!hasChanged()) {
				return;
			}
			arrLocal = obs.toArray();
			clearChanged();
		}

		for (Object obj : arrLocal) {
			((Observer) obj).update(this, arg);
		}
	}

	/**
	 * Clears the observer list so that this object no longer has any observers.
	 */
	public synchronized void deleteObservers() {
		obs.removeAllElements();
	}

	/**
	 * Returns the number of observers of this <tt>Observable</tt> object.
	 * @return the number of observers of this object.
	 */
	public synchronized int countObservers() {
		return obs.size();
	}
}
