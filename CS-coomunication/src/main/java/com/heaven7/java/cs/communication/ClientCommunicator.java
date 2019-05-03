package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.ThreadProxy;
import com.heaven7.java.meshy.Meshy;
import com.heaven7.java.meshy.Message;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.Transformers;
import com.heaven7.java.pc.consumers.SimpleConsumer;
import com.heaven7.java.pc.pm.PMS;
import com.heaven7.java.pc.producers.PipeProducer;
import com.heaven7.java.pc.schedulers.Schedulers;
import okio.*;

import java.io.IOException;

import static com.heaven7.java.cs.communication.CSConstant.TYPE_RSA_SINGLE;

/**
 * @author heaven7
 */
public final class ClientCommunicator implements Disposable, ProductContext, IMessageSender {

    public interface Connector {
        void connect() throws IOException;
        void disconnect() throws IOException;

        Sink getSink() throws IOException;
        Source getSource() throws IOException;
    }

    private static final CSThreadFactory sFACTORY = new CSThreadFactory("ClientCommunicator");
    private PipeProducer<Message<Object>> mOutProducer;
    private PMS<Message<Object>,Message<Object>> mOutService;

    private InputStreamHelper mInHelper;

    private final long mReadSleepTime;
    private final Meshy mMeshy;
    private final Connector mConnector;
    private final MessageHandler mHandler;
    private ClientMonitor mMonitor;

    public ClientCommunicator(Meshy mMeshy,Connector mConnector, MessageHandler handler, long readSleepTime) {
        this(mMeshy, mConnector, handler, readSleepTime, 8);
    }

    private ClientCommunicator(Meshy mMeshy, Connector mConnector, MessageHandler handler, long readSleepTime, int queueSize) {
        this.mMeshy = mMeshy;
        this.mConnector = mConnector;
        this.mHandler = handler;
        this.mReadSleepTime = readSleepTime;
        this.mOutProducer = new PipeProducer<>(queueSize);
    }
    public ClientMonitor getClientMonitor(){
        return mMonitor;
    }
    public void setClientMonitor(ClientMonitor mMonitor) {
        this.mMonitor = mMonitor;
    }

    public boolean start() throws IOException{
        mConnector.connect();
        //write
        mOutService = new PMS.Builder<Message<Object>, Message<Object>>()
                        .context(this)
                        .scheduler(Schedulers.io())
                        .producer(mOutProducer)
                        .transformer(Transformers.unchangeTransformer())
                        .consumer(new OutputStreamConsumer(mMeshy, mConnector.getSink()))
                        .build();
        if(!mOutService.start()){
            mMonitor.onStart(false);
            return false;
        }
        //start loop read
        mInHelper = new InputStreamHelper(this, mConnector.getSource(), mReadSleepTime);
        mInHelper.start();
        mMonitor.onStart(true);
        return true;
    }
    @Override
    public boolean sendMessage(Message<Object> msg, float version) {
        if(mOutService != null){
            mOutProducer.getPipe().addProduct(msg);
            mMonitor.onSendMessageToRemote(msg);
            return true;
        }
        return false;
    }

    @Override
    public boolean sendMessage(Message<Object> msg) {
        if(mOutService != null){
            mOutProducer.getPipe().addProduct(msg);
            mMonitor.onSendMessageToRemote(msg);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        if(mInHelper != null){
            mInHelper.dispose();
            mInHelper = null;
        }
        try {
            mConnector.disconnect();
        } catch (IOException e) {
            //ignore e.printStackTrace();
        }
        if (mOutService != null) {
            mOutService.dispose();
            mOutService = null;
        }
        mMonitor.onEnd();
    }

    private static class InputStreamHelper implements Disposable, Runnable {

        private volatile boolean disposed;
        private final BufferedSource source;
        private ThreadProxy proxy;
        private final long sleepTime;

        private ClientCommunicator communicator;

        public InputStreamHelper(ClientCommunicator communicator,Source in, long sleepTime) {
            this.communicator = communicator;
            this.sleepTime = sleepTime;
            this.source = Okio.buffer(in);
            this.proxy = ThreadProxy.create(sFACTORY);
        }
        public void start(){
            this.proxy.start(this);
        }
        @Override
        public void dispose() {
            disposed = true;
            if(proxy != null){
                proxy.dispose();
                proxy = null;
            }
        }
        @Override
        public void run() {
            try{
                Scheduler.Worker worker = Schedulers.io().newWorker();
                Meshy mMeshy = communicator.mMeshy;
                //Timeout timeout = source.timeout();
                while (!disposed){
                    /*timeout.clearDeadline();
                    timeout.deadline(5000, TimeUnit.MILLISECONDS);*/
                    Message<Object> msg = null;
                    try{
                        msg = mMeshy.getReader().readMessage(source);
                    }catch (Exception e){
                        //for dispose .we just ignore it.
                        if(!disposed){
                            communicator.mMonitor.onReadException(e);
                        }
                    }
                    if(msg == null){
                        //wait if need. or else loop until dispose
                        if(sleepTime > 0){
                            synchronized (this){
                                this.wait(sleepTime);
                            }
                        }
                    }else {
                        communicator.mMonitor.onReceiveMessage(msg);
                        worker.schedule(new InRunner(communicator, msg));
                    }
                }
            }catch (InterruptedException e){
                //ignore e.printStackTrace();
            }
        }
    }

    private static class InRunner implements Runnable{
        final ClientCommunicator communicator;
        final Message<Object> msg;
        public InRunner(ClientCommunicator communicator, Message<Object> msg) {
            this.communicator = communicator;
            this.msg = msg;
        }
        @Override
        public void run() {
            communicator.mHandler.handleMessage(communicator, msg, communicator.mMeshy.getVersion());
        }
    }

    private static class OutputStreamConsumer extends SimpleConsumer<Message<Object>> {
        private final Meshy meshy;
        private final BufferedSink sink;

        public OutputStreamConsumer( Meshy meshy, Sink out) {
            this.meshy = meshy;
            this.sink = Okio.buffer(out);
        }
        @Override
        public void onConsume(Message<Object> obj, Runnable next) {
            meshy.getWriter().writeMessage(sink, obj, TYPE_RSA_SINGLE); //need register message secure by this type
            next.run();
        }
    }
}
