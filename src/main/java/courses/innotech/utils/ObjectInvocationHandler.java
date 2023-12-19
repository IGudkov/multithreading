package courses.innotech.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ObjectInvocationHandler implements InvocationHandler{
  private Object obj;

  private Map<String,Object> hashValues = new HashMap<>();
  private Map<String, Instant> hashTime = new HashMap<>();
  public ObjectInvocationHandler(Object obj)  {
    this.obj = obj;

    // запускаем поток по очистке кэша только если есть хотя бы одна аннотация Cache с значением > 0
    boolean isThread = false;
    for (Method declaredMethod : this.obj.getClass().getDeclaredMethods()) {
      if (declaredMethod.isAnnotationPresent(Cache.class) && declaredMethod.getAnnotation(Cache.class).value() > 0) {
        isThread = true;
        break;
      }
    }

    if (isThread) {
      // создаем поток по очистке кэша
      Thread thread = new Thread(() -> {
        try {
          this.clearCache();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
      thread.start();
    }
  }

  // метод по очистке кэша, вызываемый из потока
  public void clearCache() throws InterruptedException {
    while (true) {
      Set<String> keySet = new HashSet<>();
      for (Map.Entry<String, Instant> stringInstantEntry : hashTime.entrySet()) {
        if (stringInstantEntry.getValue().isBefore(Instant.now())) {
          keySet.add(stringInstantEntry.getKey());
        }
      }
      // удаляем кэш
      if (!keySet.isEmpty()) {
        synchronized (this) {
          hashTime.keySet().removeAll(keySet);
          hashValues.keySet().removeAll(keySet);
        }
      }
      TimeUnit.SECONDS.sleep(1);
    }
  }
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Method methodObject = obj.getClass().getDeclaredMethod(method.getName(),method.getParameterTypes());

    /* аннотация Mutator не используется, т.к.:
        - либо очистка кэша производится по времени, если в аннотоции Cache указано значение
        - либо очистка кэша Не производится, если в аннотоции Cache значение Не указано
    if (methodObject.isAnnotationPresent(Mutator.class)) {
      actions.clear();
    }
    */

    if (methodObject.isAnnotationPresent(Cache.class)) {
      long cacheValue = methodObject.getAnnotation(Cache.class).value();
      //ключ для мап формируем по методу + хэшкоду объекта
      String hashKey = methodObject.toString() + String.valueOf(obj.hashCode());

      /*  если в аннотации Cache установлено значение
            - заполняем мапу со временем, по которой работает поток по чистке кэша
          если в аннотации Cache НЕ установлено значение
            - не заполняем мапу со временем, таким образом поток не увидит наше значение и закэшированное значение
              не удаляется
      */
      synchronized (this) {
        if (hashValues.containsKey(hashKey)) {
          //System.out.println("Есть кэш");
          // если в аннотации Cache установлено значение, то обновляем время жизни закэшированного значения
          if (cacheValue > 0) {
            hashTime.put(hashKey, Instant.now().plusMillis(cacheValue));
          }
          return hashValues.get(hashKey);
        }
      }

      //System.out.println("Нет кэша");
      //выполняем метод и кэшируем полученное значение
      Object returnMethodValue = method.invoke(obj, args);
      synchronized (this) {
        hashValues.put(hashKey, returnMethodValue);
        // если в аннотации Cache установлено значение, то устанавливаем время жизни закэшированного значения
        if (cacheValue > 0)
          hashTime.put(hashKey, Instant.now().plusMillis(cacheValue));
        return returnMethodValue;
      }
    }
    return method.invoke(obj, args);
  }
}
