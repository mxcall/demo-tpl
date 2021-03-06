package com.matrix.call.demo.core.demo016ForkJoin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo000
 *
 * @author weiqi
 * @create 2018-04-18 8:56:00
 */
public class ForkJoinSumRunnerFour extends RecursiveTask<Long> {


    private static final Logger LOGGER = LoggerFactory.getLogger(ForkJoinSumRunnerFour.class);

    private long start = 0L;
    private long end = 1000L;
    private long CRITICAL_FORK_VAL = 1_0000L -1L;

    public ForkJoinSumRunnerFour(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public ForkJoinSumRunnerFour(long start, long end, long CRITICAL_FORK_VAL) {
        this.start = start;
        this.end = end;
        this.CRITICAL_FORK_VAL = CRITICAL_FORK_VAL;
    }

    @Override
    protected Long compute() {
        if (this.end - this.start <= CRITICAL_FORK_VAL) {
            LOGGER.info("*开始计算: {} - {}", this.start, this.end);
            long sum = sumStartAndEnd(this.start, this.end);
            // wast time
            //wastCPU((long)(Math.random()*10000_0000_0L));
            wastCPU(10000_0000_0L);
            return sum;
        } else { // fork
            LOGGER.info("开始拆分: {} - {}", this.start, this.end);
            long cuttedEnd = this.start + CRITICAL_FORK_VAL;
            ForkJoinSumRunnerFour forkJoinSumRunner_1 = new ForkJoinSumRunnerFour(this.start, cuttedEnd);
            ForkJoinSumRunnerFour forkJoinSumRunner_2 = new ForkJoinSumRunnerFour(cuttedEnd+1, this.end);
            ForkJoinSumRunnerFour forkJoinSumRunner_3 = new ForkJoinSumRunnerFour(-1, 1);
            ForkJoinSumRunnerFour forkJoinSumRunner_4 = new ForkJoinSumRunnerFour(-1, 1);

            forkJoinSumRunner_1.fork();
            forkJoinSumRunner_2.fork();
            forkJoinSumRunner_3.fork();
            forkJoinSumRunner_4.fork();
            // **用invokeAll效率确实提高至少一倍?, 不要用xx.fork()? 暂时无法重现, 感觉都效率差不多 **
            //invokeAll(forkJoinSumRunner_1, forkJoinSumRunner_2, forkJoinSumRunner_3, forkJoinSumRunner_4);
            return forkJoinSumRunner_1.join() + forkJoinSumRunner_2.join()+ forkJoinSumRunner_3.join()+forkJoinSumRunner_4.join();

        }
    }



    public static long sumStartAndEnd(long start, long end) {
        long sum = 0L;
        for (long i = start; i <= end; i++) {
            sum = sum +i;
        }
        return sum;
    }



    public static void main(String[] args) throws InterruptedException, ExecutionException {

        long start = 1L;
        long end =  1_0000_00L;

        // fork-join
        long startTime = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(8);
        ForkJoinSumRunnerFour forkJoinSumRunner = new ForkJoinSumRunnerFour(start, end);
        Long result = pool.invoke(forkJoinSumRunner);
        long endTime = System.currentTimeMillis();
        System.out.println("####### result-fork-join: "+result);
        System.out.println("####### time-fork-join: "+ (endTime - startTime)/1000L +" s");

        // common
        Long resultCommon = sumStartAndEnd(start, end);
        System.out.println("####### result-common   : "+resultCommon);
    }

    /**
     * 休眠辅助方法
     * @param milliSecond
     */
    public static void sleep(long milliSecond) {
        try {
            if (milliSecond <= 0L) {
                return;
            }
            Thread.sleep(milliSecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void wastCPU(long val) {
        try {
            for (long t = val; t > 0; t--) {
                Math.sqrt(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
