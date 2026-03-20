package edu.hitsz.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 炸弹管理器 - 观察者模式
 */
public class BombManager implements BombSubject {
    private final List<BombObserver> observers;

    public BombManager() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void registerObserver(BombObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(BombObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (BombObserver observer : observers) {
            observer.onBombExplode();
        }
    }

    public void triggerBombExplosion() {
        notifyObservers();
    }
}
