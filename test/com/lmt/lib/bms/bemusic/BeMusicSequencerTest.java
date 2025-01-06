package com.lmt.lib.bms.bemusic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.lmt.lib.bms.BmsStandardLoader;
import com.lmt.lib.bms.Tests;

public class BeMusicSequencerTest {
	private enum Event {
		START_LOOP, FINISH_LOOP, PLAY, STOP, PAUSE, RESUME, COMPLETE, UPDATE, POINT, SEEK, GET_SLEEP_TIME;
	}

	private static class EventData {
		Event event;
		Object data;
		EventData(Event e, Object d) { event = e; data = d; }
	}

	private static class MyException extends RuntimeException {
		// Empty
	}

	private static class TestSequencer extends BeMusicSequencer {
		double sleepTime = 0.0; // 初期値はスリープなしとする
		List<EventData> eventHistories = new ArrayList<>();
		Map<Event, Consumer<Object>> eventHandlers = new HashMap<>();
		TestSequencer(BeMusicChart chart) { super(chart); }
		TestSequencer(BeMusicChart chart, double duration) { super(chart, duration); }
		@Override protected void onStartLoop() { h(Event.START_LOOP); }
		@Override protected void onFinishLoop(Throwable uncaught) { h(Event.FINISH_LOOP, uncaught); }
		@Override protected void onPlay() { h(Event.PLAY); }
		@Override protected void onStop() { h(Event.STOP); }
		@Override protected void onPause() { h(Event.PAUSE); }
		@Override protected void onResume() { h(Event.RESUME); }
		@Override protected void onComplete() { h(Event.COMPLETE); }
		@Override protected void onUpdate() { h(Event.UPDATE); }
		@Override protected void onPoint(BeMusicPoint p) { h(Event.POINT, p); }
		@Override protected void onSeek(double oldPosition) { h(Event.SEEK, oldPosition); }
		@Override protected double onGetSleepTime() { h(Event.GET_SLEEP_TIME); return sleepTime; }
		private void h(Event event) { hMain(event, null); }
		private void h(Event event, Object data) { hMain(event, data); }
		private void hMain(Event event, Object data) {
			eventHistories.add(new EventData(event, data));
			Optional.ofNullable(eventHandlers.get(event)).ifPresent(h -> h.accept(data));
		}
	}

	private static class TestThrownInStartLoop extends TestSequencer {
		TestThrownInStartLoop(BeMusicChart chart) { super(chart); }
		@Override protected void onStartLoop() { super.onStartLoop(); throw new MyException(); }
	}

	private static class TestThrownInUpdating extends TestSequencer {
		TestThrownInUpdating(BeMusicChart chart) { super(chart); }
		@Override protected void onUpdate() { super.onUpdate(); throw new MyException(); }
	}

	private static class TestLongUpdating extends TestSequencer {
		TestLongUpdating(BeMusicChart chart, double duration) { super(chart, duration); }
		@Override protected void onUpdate() { try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException e) {} }
	}

	// BeMusicSequencer(BeMusicChart)
	// 指定したBMS譜面が再生対象になり、再生時間はBMS譜面の演奏時間になること
	@Test
	public void testBeMusicSequencer1_Normal() throws Exception {
		var c = shortChart();
		var tm = c.getPlayTime();
		var s = new TestSequencer(c);
		assertSame(c, s.getChart());
		assertEquals(tm, s.getDuration(), 0.0);
	}

	// BeMusicSequencer(BeMusicChart)
	// NullPointerException chartがnull
	@Test
	public void testBeMusicSequencer1_NullChart() throws Exception {
		assertThrows(NullPointerException.class, () -> new TestSequencer(null));
	}

	// BeMusicSequencer(BeMusicChart, double)
	// 指定したBMS譜面が再生対象になり、再生時間は指定した値と等しくなること
	@Test
	public void testBeMusicSequencer2_Normal() throws Exception {
		var c = longChart();
		var tm = c.getPlayTime() + 3.0;
		var s = new TestSequencer(c, tm);
		assertSame(c, s.getChart());
		assertEquals(tm, s.getDuration(), 0.0);
	}

	// BeMusicSequencer(BeMusicChart, double)
	// NullPointerException chartがnull
	@Test
	public void testBeMusicSequencer2_NullChart() throws Exception {
		assertThrows(NullPointerException.class, () -> new TestSequencer(null, 1.0));
	}

	// BeMusicSequencer(BeMusicChart, double)
	// IllegalArgumentException durationがマイナス値
	@Test
	public void testBeMusicSequencer2_MinusDuration() throws Exception {
		var c = longChart();
		assertThrows(IllegalArgumentException.class, () -> new TestSequencer(c, Math.nextDown(0.0)));
	}

	// getChart()
	// コンストラクタで指定されたBMS譜面を返すこと
	@Test
	public void testGetChart() throws Exception {
		var c = shortChart();
		var s1 = new TestSequencer(c);
		assertSame(c, s1.getChart());
		var s2 = new TestSequencer(c, 10.0);
		assertSame(c, s2.getChart());
	}

	// getDuration()
	// コンストラクタで指定された再生時間を返すこと
	@Test
	public void testGetDuration() throws Exception {
		var c = shortChart();
		var tm = 120.567;
		var s = new TestSequencer(c, tm);
		assertEquals(tm, s.getDuration(), 0.0);
	}

	// getPosition()
	// 現在位置を返すこと
	@Test
	public void testGetPosition() throws Exception {
		var s = new TestSequencer(shortChart(), 60.0);
		assertEquals(0.0, s.getPosition(), 0.0); // 初期位置は先頭
		Tests.setf(s, "mPosition", 10.123);
		assertEquals(10.123, s.getPosition(), 0.0);
	}

	// setPosition(double)
	// 初期状態時に再生位置設定要求を行うとonSeek()が呼ばれ、引数に変更前、getPosition()で変更後の再生位置が得られること
	@Test
	public void testSetPosition_WhenIdle() throws Exception {
		var s = new TestSequencer(longChart());
		s.setPosition(1.0);
		assertEquals(0.0, s.getPosition(), 0.0); // 時間経過処理を行うまでは再生位置は更新されない
		s.update();
		assertContainsEvent(s, Event.SEEK, ed -> assertEquals(0.0, (double)ed.data, 0.0));
		assertEquals(1.0, s.getPosition(), 0.0);
	}

	// setPosition(double)
	// 再生中に再生位置設定要求を行うとonSeek()が呼ばれ、引数に変更前、getPosition()で変更後の再生位置が得られること
	@Test
	public void testSetPosition_WhenPlaying() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.setPosition(0.5);
		s.update();
		s.eventHistories.clear();
		s.setPosition(2.5);
		s.update();
		assertContainsEvent(s, Event.SEEK, ed -> assertEquals(0.5, (double)ed.data, 0.1));
		assertEquals(2.5, s.getPosition(), 0.1);
		// ※再生により時間が経過するため精度を落としてチェックする
	}

	// setPosition(double)
	// 一時停止中に再生位置設定要求を行うとonSeek()が呼ばれ、引数に変更前、getPosition()で変更後の再生位置が得られること
	@Test
	public void testSetPosition_WhenPausing() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.pause();
		s.setPosition(0.8);
		s.update();
		s.eventHistories.clear();
		s.setPosition(3.1);
		s.update();
		assertContainsEvent(s, Event.SEEK, ed -> assertEquals(0.8, (double)ed.data, 0.0));
		assertEquals(3.1, s.getPosition(), 0.0);
	}

	// setPosition(double)
	// 再生完了時に再生位置設定要求を行うと何も行われないこと
	@Test
	public void testSetPosition_WhenCompleted() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		s.play();
		s.setPosition(5.0);
		s.update();
		assertTrue(s.isCompleted());
		s.eventHistories.clear();
		s.setPosition(4.5);
		s.update();
		assertNotContainsEvent(s, Event.SEEK);
		assertEquals(5.0, s.getPosition(), 0.0);
	}

	// setPosition(double)
	// IllegalArgumentException newPositionがマイナス値または再生時間超過
	@Test
	public void testSetPosition_BadPosition() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> s.setPosition(Math.nextDown(0.0)));
		assertThrows(ex, () -> s.setPosition(Math.nextUp(5.0)));
	}

	// setPosition(double)
	// IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	@Test
	public void testSetPosition_QueueFull() throws Exception {
		var s = new TestSequencer(longChart());
		fullQueue(s);
		assertThrows(IllegalStateException.class, () -> s.setPosition(1.0));
	}

	// isPlaying()
	// 状態が再生中の時にtrueを返すこと
	@Test
	public void testIsPlaying() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		assertFalse(s.isPlaying());
		s.play();
		s.update();
		assertTrue(s.isPlaying());
		s.pause();
		s.update();
		assertFalse(s.isPlaying());
		s.setPosition(5.0);
		s.resume();
		s.update();
		assertFalse(s.isPlaying());

	}

	// isPausing()
	// 状態が一時停止中の時にtrueを返すこと
	@Test
	public void testIsPausing() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		assertFalse(s.isPausing());
		s.play();
		s.update();
		assertFalse(s.isPausing());
		s.pause();
		s.update();
		assertTrue(s.isPausing());
		s.setPosition(5.0);
		s.resume();
		s.update();
		assertFalse(s.isPausing());
	}

	// isStopped()
	// 状態が初期状態の時にtrueを返すこと
	@Test
	public void testIsStopped() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		assertTrue(s.isStopped());
		s.play();
		s.update();
		assertFalse(s.isStopped());
		s.pause();
		s.update();
		assertFalse(s.isStopped());
		s.setPosition(5.0);
		s.resume();
		s.update();
		assertFalse(s.isStopped());
	}

	// isCompleted()
	// 状態が再生完了の時にtrueを返すこと
	@Test
	public void testIsCompleted() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		assertFalse(s.isCompleted());
		s.play();
		s.update();
		assertFalse(s.isCompleted());
		s.pause();
		s.update();
		assertFalse(s.isCompleted());
		s.setPosition(5.0);
		s.resume();
		s.update();
		assertTrue(s.isCompleted());
	}

	// loop(boolean)
	// 最初にonStartLoop()が呼ばれ、最後にonFinishLoop()が呼ばれること
	@Test(timeout = 1000)
	public void testLoop_Start_Finish() throws Exception {
		var s = new TestSequencer(shortChart(), 0.001);
		s.play();
		s.loop(true);
		var ef = s.eventHistories.get(0);
		var el = s.eventHistories.get(s.eventHistories.size() - 1);
		assertEquals(Event.START_LOOP, ef.event);
		assertEquals(Event.FINISH_LOOP, el.event);
	}

	// loop(boolean)
	// onStartLoop()が例外をスローすると時間経過処理が行われずonFinishLoop()にその例外が渡り、呼び元に例外がスローされること
	@Test
	public void testLoop_ThrownInStart() throws Exception {
		var s = new TestThrownInStartLoop(shortChart());
		var ex = MyException.class; // onStartLoop()でスローしている例外
		s.play();
		assertThrows(ex, () -> s.loop(true));
		assertContainsEvent(s, Event.START_LOOP);
		assertNotContainsEvent(s, Event.POINT);
		assertNotContainsEvent(s, Event.UPDATE);
		assertContainsEvent(s, Event.FINISH_LOOP, ed -> assertEquals(ex, ed.data.getClass()));
	}

	// loop(boolean)
	// 時間経過処理で例外がスローされるとonFinishLoop()にその例外が渡り、呼び元に例外がスローされること
	@Test
	public void testLoop_ThrownInUpdating() throws Exception {
		var s = new TestThrownInUpdating(shortChart());
		var ex = MyException.class; // onUpdate()でスローしている例外
		s.play();
		assertThrows(ex, () -> s.loop(true));
		assertContainsEvent(s, Event.START_LOOP);
		assertContainsEvent(s, Event.UPDATE);
		assertContainsEvent(s, Event.FINISH_LOOP, ed -> assertEquals(ex, ed.data.getClass()));
	}

	// loop(boolean)
	// スレッド割り込みを行うとonFinishLoop()が呼ばれループが終了すること
	@Test(timeout = 1000)
	public void testLoop_Interrupt() throws Exception {
		var s = new TestSequencer(shortChart());
		var t = new Thread(() -> s.loop(false));
		t.start();
		t.interrupt();
		t.join();
		assertContainsEvent(s, Event.START_LOOP);
		assertContainsEvent(s, Event.FINISH_LOOP, ed -> assertNull(ed.data));
	}

	// loop(boolean)
	// exitWhenComplete==falseでは再生終了してもループが終了しないこと
	@Test(timeout = 1000)
	public void testLoop_NotExitByComplete() throws Exception {
		var s = new TestSequencer(shortChart(), 0.001);
		var t = new Thread(() -> s.loop(false));
		s.play();
		t.start();
		while (!s.isCompleted()) { Thread.sleep(1); }
		t.interrupt();
		t.join();
		assertContainsEvent(s, Event.START_LOOP);
		assertContainsEvent(s, Event.COMPLETE);
		assertContainsEvent(s, Event.FINISH_LOOP, ed -> assertNull(ed.data));
	}

	// loop(boolean)
	// exitWhenComplete==trueで再生終了するとループが終了すること
	@Test(timeout = 1000)
	public void testLoop_ExitByComplete() throws Exception {
		var s = new TestSequencer(shortChart(), 0.001);
		var t = new Thread(() -> s.loop(true));
		s.play();
		t.start();
		t.join();
		assertContainsEvent(s, Event.START_LOOP);
		assertContainsEvent(s, Event.COMPLETE);
		assertContainsEvent(s, Event.FINISH_LOOP, ed -> assertNull(ed.data));
	}

	// loop(boolean)
	// ループ処理中はonGetSleepTime()が呼ばれること
	@Test
	public void testLoop_GetSleepTime() throws Exception {
		var s = new TestSequencer(shortChart(), 0.005);
		var t = new Thread(() -> s.loop(true));
		s.play();
		t.start();
		t.join();
		assertContainsEvent(s, Event.GET_SLEEP_TIME);
	}

	// loop(boolean)
	// onGetSleepTime()でSLEEP_TIME_MAXを超える値を返すとonFinishLoop()にIllegalArgumentExceptionが渡り、呼び元にスローされること
	@Test
	public void testLoop_BadSleepTime() throws Exception {
		var s = new TestSequencer(shortChart(), 0.005);
		var ex = IllegalArgumentException.class;
		s.sleepTime = Math.nextUp(BeMusicSequencer.SLEEP_TIME_MAX);
		assertThrows(ex, () -> s.loop(false));
		assertContainsEvent(s, Event.START_LOOP);
		assertContainsEvent(s, Event.GET_SLEEP_TIME);
		assertContainsEvent(s, Event.FINISH_LOOP, ed -> assertEquals(ex, ed.data.getClass()));
	}

	// loop(boolean)
	// ループ開始後に先頭から再生し、再生が終了するまでのイベント呼び出し順が期待通りであること
	@Test(timeout = 1000)
	public void testLoop_EventCallSequence() throws Exception {
		var s = new TestSequencer(shortChart(), 0.04);
		s.play();
		s.loop(true);
		var evs = s.eventHistories.stream().filter(ed -> ed.event != Event.UPDATE && ed.event != Event.GET_SLEEP_TIME)
				.collect(Collectors.toList());
		assertEquals(8, evs.size());
		assertEquals(Event.START_LOOP, evs.get(0).event);
		assertEquals(Event.PLAY, evs.get(1).event);
		assertEquals(Event.POINT, evs.get(2).event);
		assertEquals(0.00, ((BeMusicPoint)evs.get(2).data).getTime(), 0.0);
		assertEquals(Event.POINT, evs.get(3).event);
		assertEquals(0.01, ((BeMusicPoint)evs.get(3).data).getTime(), 0.0);
		assertEquals(Event.POINT, evs.get(4).event);
		assertEquals(0.02, ((BeMusicPoint)evs.get(4).data).getTime(), 0.0);
		assertEquals(Event.POINT, evs.get(5).event);
		assertEquals(0.03, ((BeMusicPoint)evs.get(5).data).getTime(), 0.0);
		assertEquals(Event.COMPLETE, evs.get(6).event);
		assertEquals(Event.FINISH_LOOP, evs.get(7).event);
		assertNull(evs.get(7).data);
	}

	// loop(boolean)
	// IllegalStateException 別スレッドがループ処理を実行中
	@Test(timeout = 1000)
	public void testLoop_AlreadyRunningLoop() throws Exception {
		var s = new TestSequencer(shortChart(), 0.001);
		var t = new Thread(() -> { s.play(); s.loop(false); });
		t.start();
		while (!s.isCompleted()) { Thread.sleep(1); }
		assertThrows(IllegalStateException.class, () -> s.loop(false));
		t.interrupt();
		t.join();
	}

	// loop(boolean)
	// IllegalStateException update()を実行中
	@Test(timeout = 1000)
	public void testLoop_AlreadyRunningUpdate() throws Exception {
		var s = new TestLongUpdating(shortChart(), 0.001);
		var t = new Thread(() -> { s.play(); s.update(); });
		t.start();
		while (!s.isPlaying()) { Thread.sleep(1); }
		assertThrows(IllegalStateException.class, () -> s.loop(false));
		t.interrupt();
		t.join();
	}

	// update()
	// 初期状態時に実行すると、時間経過せずonUpdate()が呼ばれること
	@Test
	public void testUpdate_WhenIdle() throws Exception {
		var s = new TestSequencer(shortChart(), 1.0);
		s.update();
		assertContainsEvent(s, Event.UPDATE);
		assertNotContainsEvent(s, Event.POINT);
		s.setPosition(1.0);
		s.update();
		assertContainsEvent(s, Event.SEEK);
		assertNotContainsEvent(s, Event.COMPLETE);
	}

	// update()
	// 再生中に実行すると、時間経過処理が行われ、onUpdate()が呼ばれること
	@Test
	public void testUpdate_WhenPlaying() throws Exception {
		var s = new TestSequencer(shortChart(), 1.0);
		s.play();
		s.update();
		assertContainsEvent(s, Event.UPDATE);
		assertContainsEvent(s, Event.POINT, ed -> assertEquals(0.0, ((BeMusicPoint)ed.data).getTime(), 0.0));
		s.setPosition(1.0);
		s.update();
		assertContainsEvent(s, Event.SEEK);
		assertContainsEvent(s, Event.COMPLETE);
	}

	// update()
	// 一時停止中に実行すると、時間経過せずonUpdate()が呼ばれること
	@Test
	public void testUpdate_WhenPausing() throws Exception {
		var s = new TestSequencer(shortChart(), 1.0);
		s.play();
		s.pause();
		s.update();
		assertContainsEvent(s, Event.UPDATE);
		assertNotContainsEvent(s, Event.POINT);
		s.setPosition(1.0);
		s.update();
		assertContainsEvent(s, Event.SEEK);
		assertNotContainsEvent(s, Event.COMPLETE);
	}

	// update()
	// 再生完了時に実行すると、時間経過せずonUpdate()が呼ばれること
	@Test
	public void testUpdate_WhenCompleted() throws Exception {
		var s = new TestSequencer(shortChart(), 1.0);
		s.setPosition(1.0);
		s.play();
		s.update();
		assertTrue(s.isCompleted());
		s.eventHistories.clear();
		s.update();
		assertContainsEvent(s, Event.UPDATE);
	}

	// update()
	// onGetSleepTime()は呼ばれず待機処理が行われないこと
	@Test
	public void testUpdate_NoCallGetSleepTime() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		s.update();
		assertNotContainsEvent(s, Event.GET_SLEEP_TIME);
		s.play();
		s.update();
		assertNotContainsEvent(s, Event.GET_SLEEP_TIME);
		s.pause();
		s.update();
		assertNotContainsEvent(s, Event.GET_SLEEP_TIME);
		s.resume();
		s.update();
		assertNotContainsEvent(s, Event.GET_SLEEP_TIME);
		s.setPosition(5.0);
		s.update();
		assertNotContainsEvent(s, Event.GET_SLEEP_TIME);
	}

	// update()
	// 時間経過処理により楽曲位置を通過すると、onPoint()で当該楽曲位置情報が通知され、その後onUpdate()が呼ばれること
	@Test(timeout = 1000)
	public void testUpdate_SinglePoint() throws Exception {
		var s = new TestSequencer(shortChart());
		s.play();
		s.update();
		var evs = s.eventHistories;
		assertEquals(3, evs.size());
		assertEquals(Event.PLAY, evs.get(0).event);
		assertEquals(Event.POINT, evs.get(1).event);
		assertEquals(0.0, ((BeMusicPoint)evs.get(1).data).getTime(), 0.0);
		assertEquals(Event.UPDATE, evs.get(2).event);
	}

	// update()
	// 時間経過処理により楽曲位置を複数通過すると、通過数分onPoint()が時間が古い順に呼ばれ、その後onUpdate()が呼ばれること
	@Test
	public void testUpdate_MultiplePoints() throws Exception {
		var s = new TestSequencer(shortChart(), 0.02);
		s.play();
		s.update();
		Thread.sleep(30);
		s.update();
		var evs = s.eventHistories;
		assertEquals(7, evs.size());
		assertEquals(Event.PLAY, evs.get(0).event);
		assertEquals(Event.POINT, evs.get(1).event);
		assertEquals(0.00, ((BeMusicPoint)evs.get(1).data).getTime(), 0.0);
		assertEquals(Event.UPDATE, evs.get(2).event);
		assertEquals(Event.POINT, evs.get(3).event);
		assertEquals(0.01, ((BeMusicPoint)evs.get(3).data).getTime(), 0.0);
		assertEquals(Event.POINT, evs.get(4).event);
		assertEquals(0.02, ((BeMusicPoint)evs.get(4).data).getTime(), 0.0);
		assertEquals(Event.UPDATE, evs.get(5).event);
		assertEquals(Event.COMPLETE, evs.get(6).event);
	}

	// update()
	// 時間経過処理により再生時間に到達すると、onUpdate()が呼ばれた後でonComplete()が呼ばれること
	@Test
	public void testUpdate_Complete() throws Exception {
		var s = new TestSequencer(shortChart(), 1.0);
		s.setPosition(1.0);
		s.play();
		s.update();
		var evs = s.eventHistories;
		assertEquals(4, evs.size());
		assertEquals(Event.SEEK, evs.get(0).event);
		assertEquals(Event.PLAY, evs.get(1).event);
		assertEquals(Event.UPDATE, evs.get(2).event);
		assertEquals(Event.COMPLETE, evs.get(3).event);
	}

	// update()
	// IllegalStateException 別スレッドが時間経過処理を実行中
	@Test(timeout = 1000)
	public void testUpdate_AlreadyRunningUpdate() throws Exception {
		var s = new TestLongUpdating(shortChart(), 0.001);
		var t = new Thread(() -> { s.play(); s.update(); });
		t.start();
		while (!s.isPlaying()) { Thread.sleep(1); }
		assertThrows(IllegalStateException.class, () -> s.update());
		t.interrupt();
		t.join();
	}

	// update()
	// IllegalStateException loop(boolean)を実行中
	@Test(timeout = 1000)
	public void testUpdate_AlreadyRunningLoop() throws Exception {
		var s = new TestSequencer(shortChart());
		var t = new Thread(() -> { s.play(); s.loop(false); });
		t.start();
		while (s.isStopped()) { Thread.sleep(1); }
		assertThrows(IllegalStateException.class, () -> s.update());
		t.interrupt();
		t.join();
	}

	// play()
	// 初期状態時に再生要求を行うと再生中に遷移し、onPlay()が呼ばれること
	@Test
	public void testPlay_WhenIdle() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.update();
		assertTrue(s.isPlaying());
		assertContainsEvent(s, Event.PLAY);
	}

	// play()
	// 再生中に再生要求を行うと何も行われないこと
	@Test
	public void testPlay_WhenPlaying() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.update();
		s.eventHistories.clear();
		s.play();
		s.update();
		assertTrue(s.isPlaying());
		assertNotContainsEvent(s, Event.PLAY);
	}

	// play()
	// 一時停止中に再生要求を行うと何も行われないこと
	@Test
	public void testPlay_WhenPausing() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.pause();
		s.update();
		s.eventHistories.clear();
		s.play();
		s.update();
		assertFalse(s.isPlaying());
		assertNotContainsEvent(s, Event.PLAY);
	}

	// play()
	// 再生完了時に再生要求を行うと何も行われないこと
	@Test
	public void testPlay_WhenCompleted() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		s.setPosition(5.0);
		s.play();
		s.update();
		s.eventHistories.clear();
		s.play();
		s.update();
		assertTrue(s.isCompleted());
		assertFalse(s.isPlaying());
		assertNotContainsEvent(s, Event.PLAY);
	}

	// play()
	// 再生位置が先頭位置でない場所からの再生が想定通り動作すること
	@Test
	public void testPlay_NotHeadPosition() throws Exception {
		var s = new TestSequencer(longChart());
		s.setPosition(1.5);
		s.play();
		s.update();
		assertTrue(s.isPlaying());
		assertTrue(s.getPosition() > 1.5);
		assertContainsEvent(s, Event.PLAY);
	}

	// play()
	// IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	@Test
	public void testPlay_QueueFull() throws Exception {
		var s = new TestSequencer(longChart());
		fullQueue(s);
		assertThrows(IllegalStateException.class, () -> s.play());
	}

	// stop()
	// 初期状態時に停止要求を行うと何も行われないこと
	@Test
	public void testStop_WhenIdle() throws Exception {
		var s = new TestSequencer(longChart());
		s.stop();
		s.update();
		assertTrue(s.isStopped());
		assertNotContainsEvent(s, Event.STOP);
	}

	// stop()
	// 再生中に停止要求を行うと初期状態に遷移し、再生位置が0になり、onStop()が呼ばれること
	@Test
	public void testStop_WhenPlaying() throws Exception {
		var s = new TestSequencer(longChart());
		s.setPosition(0.5);
		s.play();
		s.update();
		s.eventHistories.clear();
		s.stop();
		s.update();
		assertTrue(s.isStopped());
		assertContainsEvent(s, Event.STOP);
		assertEquals(0.0, s.getPosition(), 0.0);
	}

	// stop()
	// 一時停止中に停止要求を行うと初期状態に遷移し、再生位置が0になり、onStop()が呼ばれること
	@Test
	public void testStop_WhenPausing() throws Exception {
		var s = new TestSequencer(longChart());
		s.setPosition(0.5);
		s.play();
		s.pause();
		s.update();
		s.eventHistories.clear();
		s.stop();
		s.update();
		assertTrue(s.isStopped());
		assertContainsEvent(s, Event.STOP);
		assertEquals(0.0, s.getPosition(), 0.0);
	}

	// stop()
	// 再生完了時に停止要求を行うと初期状態に遷移し、再生位置が0になり、onStop()が呼ばれること
	@Test
	public void testStop_WhenCompleted() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		s.setPosition(5.0);
		s.play();
		s.update();
		assertTrue(s.isCompleted());
		s.eventHistories.clear();
		s.stop();
		s.update();
		assertTrue(s.isStopped());
		assertContainsEvent(s, Event.STOP);
		assertEquals(0.0, s.getPosition(), 0.0);
	}

	// stop()
	// IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	@Test
	public void testStop_QueueFull() throws Exception {
		var s = new TestSequencer(longChart());
		fullQueue(s);
		assertThrows(IllegalStateException.class, () -> s.stop());
	}

	// pause()
	// 初期状態時に一時停止要求を行うと何も行われないこと
	@Test
	public void testPause_WhenIdle() throws Exception {
		var s = new TestSequencer(longChart());
		s.pause();
		s.update();
		assertFalse(s.isPausing());
		assertNotContainsEvent(s, Event.PAUSE);
	}

	// pause()
	// 再生中に一時停止要求を行うと一時停止に遷移し、onPause()が呼ばれること
	@Test
	public void testPause_WhenPlaying() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.update();
		s.eventHistories.clear();
		s.pause();
		s.update();
		assertTrue(s.isPausing());
		assertContainsEvent(s, Event.PAUSE);
	}

	// pause()
	// 一時停止中に一時停止要求を行うと何も行われないこと
	@Test
	public void testPause_WhenPausing() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.pause();
		s.update();
		s.eventHistories.clear();
		s.pause();
		s.update();
		assertTrue(s.isPausing());
		assertNotContainsEvent(s, Event.PAUSE);
	}

	// pause()
	// 再生完了時に一時停止要求を行うと何も行われないこと
	@Test
	public void testPause_WhenCompleted() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		s.setPosition(5.0);
		s.play();
		s.update();
		assertTrue(s.isCompleted());
		s.eventHistories.clear();
		s.pause();
		s.update();
		assertFalse(s.isPausing());
		assertNotContainsEvent(s, Event.PAUSE);
		assertEquals(5.0, s.getPosition(), 0.0);
	}

	// pause()
	// 一時停止中に時間経過処理を行っても再生位置が変化しないこと
	@Test
	public void testPause_NonTimeProgress() throws Exception {
		var s = new TestSequencer(longChart());
		s.setPosition(1.5);
		s.play();
		s.pause();
		s.update();
		s.eventHistories.clear();
		var pos = s.getPosition();
		Thread.sleep(1);
		s.update();
		assertTrue(s.isPausing());
		assertEquals(pos, s.getPosition(), 0.0);
	}

	// pause()
	// IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	@Test
	public void testPause_QueueFull() throws Exception {
		var s = new TestSequencer(longChart());
		fullQueue(s);
		assertThrows(IllegalStateException.class, () -> s.pause());
	}

	// resume()
	// 初期状態時に再開要求を行うと何も行われないこと
	@Test
	public void testResume_WhenIdle() throws Exception {
		var s = new TestSequencer(longChart());
		s.resume();
		s.update();
		assertFalse(s.isPlaying());
		assertNotContainsEvent(s, Event.RESUME);
	}

	// resume()
	// 再生中に再開要求を行うと何も行われないこと
	@Test
	public void testResume_WhenPlaying() throws Exception {
		var s = new TestSequencer(longChart());
		s.play();
		s.update();
		s.eventHistories.clear();
		s.resume();
		s.update();
		assertTrue(s.isPlaying());
		assertNotContainsEvent(s, Event.RESUME);
	}

	// resume()
	// 一時停止中に再開要求を行うと再生中に遷移し、onResume()が呼ばれ、時間経過処理を行うと再生位置が変化すること
	@Test
	public void testResume_WhenPausing() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		s.setPosition(2.5);
		s.play();
		s.pause();
		s.update();
		s.eventHistories.clear();
		var pos = s.getPosition();
		s.resume();
		s.update();
		Thread.sleep(1);
		s.update();
		assertTrue(s.isPlaying());
		assertContainsEvent(s, Event.RESUME);
		assertTrue(s.getPosition() > pos);
	}

	// resume()
	// 再生完了時に再開要求を行うと何も行われないこと
	@Test
	public void testResume_WhenCompleted() throws Exception {
		var s = new TestSequencer(longChart(), 5.0);
		s.setPosition(5.0);
		s.play();
		s.update();
		assertTrue(s.isCompleted());
		s.eventHistories.clear();
		s.resume();
		s.update();
		assertFalse(s.isPlaying());
		assertNotContainsEvent(s, Event.RESUME);
	}

	// resume()
	// IllegalStateException 再生制御要求キューに空きがなく要求が受理できなかった
	@Test
	public void testResume_QueueFull() throws Exception {
		var s = new TestSequencer(longChart());
		fullQueue(s);
		assertThrows(IllegalStateException.class, () -> s.resume());
	}

	// play(BeMusicChart, Consumer<BeMusicPoint>)
	// 指定BMS譜面の最後の操作可能ノート(getPlayTime())までprocessorに通知され、その後メソッドが終了すること
	@Test(timeout = 1000)
	public void testStaticPlay1_Normal() throws Exception {
		var pts = new ArrayList<BeMusicPoint>();
		BeMusicSequencer.play(shortChart(), pts::add);
		assertEquals(3, pts.size());
		assertEquals(0.00, pts.get(0).getTime(), 0.0);
		assertEquals(0.01, pts.get(1).getTime(), 0.0);
		assertEquals(0.02, pts.get(2).getTime(), 0.0);
	}

	// play(BeMusicChart, Consumer<BeMusicPoint>)
	// NullPointerException chartがnull
	@Test
	public void testStaticPlay1_NullChart() throws Exception {
		assertThrows(NullPointerException.class, () -> BeMusicSequencer.play(null, p -> {}));
	}

	// play(BeMusicChart, Consumer<BeMusicPoint>)
	// NullPointerException processorがnull
	@Test
	public void testStaticPlay1_NullProcessor() throws Exception {
		assertThrows(NullPointerException.class, () -> BeMusicSequencer.play(shortChart(), null));
	}

	// play(BeMusicChart, double, double, Consumer<BeMusicPoint>)
	// 指定BMS譜面が再生時間(最後の操作可能ノートより手前)までprocessorに通知され、その後メソッドが終了すること
	@Test(timeout = 1000)
	public void testStaticPlay2_BeforeLastPlayable() throws Exception {
		var pts = new ArrayList<BeMusicPoint>();
		BeMusicSequencer.play(shortChart(), Math.nextDown(0.02), 0.001, pts::add);
		assertEquals(2, pts.size());
		assertEquals(0.00, pts.get(0).getTime(), 0.0);
		assertEquals(0.01, pts.get(1).getTime(), 0.0);
	}

	// play(BeMusicChart, double, double, Consumer<BeMusicPoint>)
	// 指定BMS譜面が再生時間(最後の操作可能ノートより後ろ)までprocessorに通知され、その後メソッドが終了すること
	@Test(timeout = 1000)
	public void testStaticPlay2_AfterLastPlayable() throws Exception {
		var pts = new ArrayList<BeMusicPoint>();
		BeMusicSequencer.play(shortChart(), 0.03, 0.001, pts::add);
		assertEquals(4, pts.size());
		assertEquals(0.00, pts.get(0).getTime(), 0.0);
		assertEquals(0.01, pts.get(1).getTime(), 0.0);
		assertEquals(0.02, pts.get(2).getTime(), 0.0);
		assertEquals(0.03, pts.get(3).getTime(), 0.0);
	}

	// play(BeMusicChart, double, double, Consumer<BeMusicPoint>)
	// NullPointerException chartがnull
	@Test
	public void testStaticPlay2_NullChart() throws Exception {
		assertThrows(NullPointerException.class, () -> BeMusicSequencer.play(null, 1.0, 0.001, p -> {}));
	}

	// play(BeMusicChart, double, double, Consumer<BeMusicPoint>)
	// IllegalArgumentException durationがマイナス値
	@Test
	public void testStaticPlay2_MinusDuration() throws Exception {
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BeMusicSequencer.play(shortChart(), Math.nextDown(0.0), 0.001, p -> {}));
	}

	// play(BeMusicChart, double, double, Consumer<BeMusicPoint>)
	// IllegalArgumentException sleepTimeがSLEEP_TIME_MAX超過
	@Test
	public void testStaticPlay2_BadSleepTime() throws Exception {
		var sleepTime = Math.nextUp(BeMusicSequencer.SLEEP_TIME_MAX);
		var ex = IllegalArgumentException.class;
		assertThrows(ex, () -> BeMusicSequencer.play(shortChart(), 1.0, sleepTime, p -> {}));
	}

	// play(BeMusicChart, double, double, Consumer<BeMusicPoint>)
	// NullPointerException processorがnull
	@Test
	public void testStaticPlay2_NullProcessor() throws Exception {
		assertThrows(NullPointerException.class, () -> BeMusicSequencer.play(shortChart(), 1.0, 0.001, null));
	}

	// Sequencer-status-in-events
	// onStartLoop(): 実行スレッド=時間経過処理スレッド
	@Test(timeout = 1000)
	public void testSequencerStatusInEvents_StartLoop() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(shortChart(), 0.001);
		s.eventHandlers.put(Event.START_LOOP, o -> assertEquals(t, Thread.currentThread()));
		s.play();
		s.loop(true);
		assertContainsEvent(s, Event.START_LOOP);
	}

	// Sequencer-status-in-events
	// onFinishLoop(): 実行スレッド=時間経過処理スレッド
	@Test(timeout = 1000)
	public void testSequencerStatusInEvents_FinishLoop() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(shortChart(), 0.001);
		s.eventHandlers.put(Event.FINISH_LOOP, o -> assertEquals(t, Thread.currentThread()));
		s.play();
		s.loop(true);
		assertContainsEvent(s, Event.FINISH_LOOP);
	}

	// Sequencer-status-in-events
	// onPlay(): 実行スレッド=時間経過処理スレッド, シーケンサ状態=再生中
	@Test
	public void testSequencerStatusInEvents_Play() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(shortChart(), 0.001);
		s.eventHandlers.put(Event.PLAY, o -> {
			assertEquals(t, Thread.currentThread());
			assertTrue(s.isPlaying());
		});
		s.play();
		s.update();
		assertContainsEvent(s, Event.PLAY);
	}

	// Sequencer-status-in-events
	// onStop(): 実行スレッド=時間経過処理スレッド, シーケンサ状態=初期状態
	@Test
	public void testSequencerStatusInEvents_Stop() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(longChart());
		s.play();
		s.update();
		s.eventHistories.clear();
		s.eventHandlers.put(Event.STOP, o -> {
			assertEquals(t, Thread.currentThread());
			assertTrue(s.isStopped());
		});
		s.stop();
		s.update();
		assertContainsEvent(s, Event.STOP);
	}

	// Sequencer-status-in-events
	// onPause(): 実行スレッド=時間経過処理スレッド, シーケンサ状態=一時停止中
	@Test
	public void testSequencerStatusInEvents_Pause() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(longChart());
		s.play();
		s.update();
		s.eventHistories.clear();
		s.eventHandlers.put(Event.PAUSE, o -> {
			assertEquals(t, Thread.currentThread());
			assertTrue(s.isPausing());
		});
		s.pause();
		s.update();
		assertContainsEvent(s, Event.PAUSE);
	}

	// Sequencer-status-in-events
	// onResume(): (): 実行スレッド=時間経過処理スレッド, シーケンサ状態=再生中
	@Test
	public void testSequencerStatusInEvents_Resume() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(longChart());
		s.play();
		s.pause();
		s.update();
		s.eventHistories.clear();
		s.eventHandlers.put(Event.RESUME, o -> {
			assertEquals(t, Thread.currentThread());
			assertTrue(s.isPlaying());
		});
		s.resume();
		s.update();
		assertContainsEvent(s, Event.RESUME);
	}

	// Sequencer-status-in-events
	// onComplete(): 実行スレッド=時間経過処理スレッド, シーケンサ状態=再生終了
	@Test
	public void testSequencerStatusInEvents_Complete() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(longChart(), 5.0);
		s.eventHandlers.put(Event.COMPLETE, o -> {
			assertEquals(t, Thread.currentThread());
			assertTrue(s.isCompleted());
		});
		s.setPosition(5.0);
		s.play();
		s.update();
		assertContainsEvent(s, Event.COMPLETE);
	}

	// Sequencer-status-in-events
	// onUpdate(): 実行スレッド=時間経過処理スレッド
	@Test
	public void testSequencerStatusInEvents_Update() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(longChart());
		s.eventHandlers.put(Event.UPDATE, o -> assertEquals(t, Thread.currentThread()));
		s.play();
		s.update();
		assertContainsEvent(s, Event.UPDATE);
	}

	// Sequencer-status-in-events
	// onPoint(): 実行スレッド=時間経過処理スレッド, シーケンサ状態=再生中
	@Test
	public void testSequencerStatusInEvents_Point() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(shortChart());
		s.eventHandlers.put(Event.POINT, o -> {
			assertEquals(t, Thread.currentThread());
			assertTrue(s.isPlaying());
		});
		s.play();
		s.update();
		assertContainsEvent(s, Event.POINT);
	}

	// Sequencer-status-in-events
	// onSeek(): 実行スレッド=時間経過処理スレッド
	@Test
	public void testSequencerStatusInEvents_Seek() throws Exception {
		var t = Thread.currentThread();
		var s = new TestSequencer(longChart());
		s.eventHandlers.put(Event.SEEK, o -> assertEquals(t, Thread.currentThread()));
		s.setPosition(2.5);
		s.update();
		assertContainsEvent(s, Event.SEEK);
	}

	// Sequencer-status-in-events
	// onGetSleepTime(): 実行スレッド=時間経過処理スレッド
	@Test(timeout = 1000)
	public void testSequencerStatusInEvents_GetSleepTime() throws Exception {
		var s = new TestSequencer(longChart());
		var t = new Thread(() -> s.loop(false));
		var c = new CountDownLatch(1);
		s.eventHandlers.put(Event.GET_SLEEP_TIME, o -> {
			assertEquals(t, Thread.currentThread());
			c.countDown();
		});
		t.start();
		c.await();
		t.interrupt();
		t.join();
		assertContainsEvent(s, Event.GET_SLEEP_TIME);
	}

	// 0小節目0tickから、SW11に10ミリ秒間隔で3回BEAT、その10ミリ秒後に1回BGM
	// BeMusicChart#getPlayTime()は0.02、最後の楽曲位置情報のTimeは0.03の想定
	private static BeMusicChart shortChart() throws Exception {
		return createChart(
				"#PLAYER 1",
				"#TITLE shortChart",
				"#BPM 1500",
				"#00001:00000001000000000000000000000000",
				"#00011:01010100000000000000000000000000");
	}

	// SW11に1秒間隔で4回BEAT、その1秒後に1回BGM
	// BeMusicChart#getPlayTime()は4.0、最後の楽曲位置情報のTimeは5.0の想定
	private static BeMusicChart longChart() throws Exception {
		return createChart(
				"#PLAYER 1",
				"#TITLE longChart",
				"#BPM 120",
				"#00011:0001",
				"#00111:0101",
				"#00201:0001",
				"#00211:01");
	}

	// 標準フォーマットのBMSからBMS譜面を生成する
	private static BeMusicChart createChart(String...lines) throws Exception {
		var bms = Stream.of(lines).collect(Collectors.joining("\n"));
		var content = new BmsStandardLoader().setSpec(BeMusicSpec.LATEST).setStrictly(true).load(bms);
		return BeMusicChartBuilder.createChart(content);
	}

	private static void fullQueue(TestSequencer s) {
		for (var i = 0; i < BeMusicSequencer.QUEUE_SIZE; i++) {
			s.stop();
		}
	}

	private static void assertContainsEvent(TestSequencer s, Event e) {
		if (!s.eventHistories.stream().anyMatch(ed -> ed.event == e)) {
			fail(String.format("Expect contains '%s', but not found", e));
		}
	}

	private static void assertContainsEvent(TestSequencer s, Event e, Consumer<EventData> action) {
		var found = s.eventHistories.stream().filter(ed -> ed.event == e).findFirst();
		if (!found.isPresent()) {
			fail(String.format("Expect contains '%s', but not found", e));
		} else {
			action.accept(found.get());
		}
	}

	private static void assertNotContainsEvent(TestSequencer s, Event e) {
		if (s.eventHistories.stream().anyMatch(ed -> ed.event == e)) {
			fail(String.format("Expect not contains '%s', but found", e));
		}
	}
}
