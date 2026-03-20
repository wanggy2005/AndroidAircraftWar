package edu.hitsz.observer;

public interface BombSubject {
    void registerObserver(BombObserver observer);
    void removeObserver(BombObserver observer);
    void notifyObservers();
}
