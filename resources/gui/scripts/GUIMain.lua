EventBus = luajava.bindClass("engine.core.EventBus")
--GUI = luajava.bindClass("engine.ux.GUI")

EventBus:broadcast({"GUILoadData"})
EventBus:broadcast({"GUIShow", "SoundBoard"})