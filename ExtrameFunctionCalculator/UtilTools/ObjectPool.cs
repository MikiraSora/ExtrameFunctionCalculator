using System;
using System.Collections.Generic;
using System.Collections.Concurrent;
using System.Diagnostics;

namespace ExtrameFunctionCalculator.UtilTools
{
    public interface IObjectPoolCachable
    {
        void Release();
    }

    public class ObjectPool<T> where T : class
    {
        public delegate T CreateNewObjectFunc();
        public delegate void ResetObjectFunc(T obj);
        public delegate void SetObjectValueFunc(T obj);

        Queue<T> pool;

        CreateNewObjectFunc create_function;

        ResetObjectFunc reset_function;

        int capacity;

        public ObjectPool(CreateNewObjectFunc create_func,ResetObjectFunc reset_func,int capacity=100)
        {
            if (create_func == null || reset_func == null || capacity <= 0)
                throw new Exception("Invaid Parameters");

            this.capacity = capacity;
            this.create_function = create_func;
            this.reset_function = reset_func;

            pool = new Queue<T>(capacity);

            for (int i = 0; i < capacity/3; i++)
            {
                T obj = create_function();
                reset_function(obj);
                pool.Enqueue(obj);
            }
        }

        public void Push(T item)
        {
            lock (pool)
            {
                if (item != null && pool.Count >= capacity)
                    return;
                pool.Enqueue(item);
                //Debug.Print($"push obj {item.GetHashCode()} , now capacity is {pool.Count}");
            }
        }

        public T Pop(SetObjectValueFunc settter_func=null)
        {
            T obj;
            lock (pool)
            {
                obj = pool.Count == 0 ? create_function() : pool.Dequeue();
                //Debug.Print($"pop a {(pool.Count==0?"new":"cached")} obj {obj.GetHashCode()}, now capacity is {pool.Count}");
            }

            if (settter_func == null)
                reset_function(obj);
            else
                settter_func(obj);
            return obj;
        }

        public override string ToString()
        {
            return $"Cached/Capacity : {pool.Count}/{capacity}";
        }
    }
}