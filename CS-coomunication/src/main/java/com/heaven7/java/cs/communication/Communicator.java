package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.pc.Consumer;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.Transformer;
import com.heaven7.java.pc.pm.PMS;
import com.heaven7.java.pc.producers.PipeProducer;
import com.heaven7.java.pc.schedulers.Schedulers;

/**
 * 1, 建立连接
 * 2, cs认证 ->返回一个token
 * 3. 开启tick.
 * 4, ...wait for break.
 * 5, 重连机制
 * @param <PT>
 * @param <CT>
 */
public final class Communicator<PT, CT> implements Disposable,ProductContext {

    private final PMS<PT, CT> mInService;
    private PipeProducer.Pipe<PT> mInPip;

    public Communicator(int queueSize, Transformer<? super PT, CT> transformer, Consumer<? super CT> consumer) {
        PipeProducer<PT> producer = new PipeProducer<PT>(queueSize);
        mInPip = producer.getPipe();
        mInService = new PMS.Builder<PT, CT>()
                .context(this)
                .scheduler(Schedulers.io())
                .producer(producer)
                .transformer(transformer)
                .consumer(consumer)
                .build();
        mInService.start();
    }

    public boolean addProduct(PT product){
        if(mInPip != null){
            mInPip.addProduct(product);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        if(mInPip != null){
            mInPip.close();
            mInPip = null;
            mInService.dispose();
        }
    }
}
