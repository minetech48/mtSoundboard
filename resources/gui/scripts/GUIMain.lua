EventBus = luajava.bindClass("engine.core.EventBus")

EventBus:broadcast({"GUILoadData"})
EventBus:broadcast({"GUIShow", "MainMenu"})