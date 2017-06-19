using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.UtilTools
{
    public interface IObjectPoolCachable
    {
        void Release();
    }

    public static class ObjectPool
    {
        static Dictionary<Type, Stack<object>> StoreObjectMap = new Dictionary<Type, Stack<object>>();

        public static int Capacity { get; set; } = 100;

        public delegate T OnCreateNewFunc<T>();

        public static T GetObject<T>(OnCreateNewFunc<T> GenObjectFunction)
        {
            Type type = typeof(T);
            if (!StoreObjectMap.ContainsKey(type))
                StoreObjectMap.Add(type, new Stack<object>());
            if (StoreObjectMap[type].Count == 0)
                StoreObjectMap[type].Push(GenObjectFunction());
            return (T)StoreObjectMap[type].Pop();
        }

        public static void ReleaseObject<T>(T obj)
        {
            Type type = typeof(T);
            if (IsFull(obj))
                return;
            StoreObjectMap[type].Push(obj);
        }

        public static bool IsFull<T>(T obj)
        {
            Type type = typeof(T);
            if (!StoreObjectMap.ContainsKey(type))
                StoreObjectMap.Add(type, new Stack<object>());
            return StoreObjectMap[type].Count >= Capacity;
        }
    }
}
