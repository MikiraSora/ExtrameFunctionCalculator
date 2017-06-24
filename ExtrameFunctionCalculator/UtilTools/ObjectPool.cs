using System;
using System.Collections.Generic;
using System.Collections.Concurrent;

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
        }

        public void Push(T item)
        {
            if (item != null&&pool.Count>=capacity)
                return;
            lock (pool)
            {
                pool.Enqueue(item);
            }
        }

        public T Pop()
        {
            T obj;
            lock (pool)
            {
                obj = pool.Count == 0 ? create_function() : pool.Dequeue();
            }
            reset_function(obj);
            return obj;
        }
    }
}