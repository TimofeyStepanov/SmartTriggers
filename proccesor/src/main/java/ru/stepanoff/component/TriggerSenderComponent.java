package ru.stepanoff.component;

public interface TriggerSenderComponent {
    void send(long userId);
    void setTriggerInterval(long triggerIntervalInSeconds);
}
