package play.modules.hazelcast;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import play.Logger;
import play.PlayPlugin;
import play.inject.BeanSource;
import play.inject.Injector;
import play.inject.NamedBeanSource;
import play.inject.NamedInjector;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Router;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.Transaction;

public class HazelcastPlugin extends PlayPlugin implements BeanSource, NamedBeanSource {

	private static HazelcastInstance instance;

	@Override
	public void onApplicationStart() {
		try {
			if (instance == null) {
				instance = Hazelcast.getDefaultInstance();
				if(!instance.getLifecycleService().isRunning()){
					Logger.info("Hazelcast Services is restarting...\n");
					instance.getLifecycleService().restart();
				}
				Logger.info("Hazelcast Services are now started...\n");
			}

		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
		Injector.inject(this);
		NamedInjector.inject(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onApplicationStop() {
		try {
			instance.shutdown();
			instance = null;
			Logger.info("Hazelcast Services are now stopped\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean rawInvocation(Request request, Response response) throws Exception {
		if ("/@cache".equals(request.path)) {
			response.status = 302;
			response.setHeader("Location", "/@cache/");
			return true;
		}
		return false;
	}

	@Override
	public void onRoutesLoaded() {
		Router.prependRoute("GET", "/@cache/?", "HazelcastApplication.index");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see play.inject.BeanSource#getBeanOfType(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBeanOfType(Class<T> clazz) {
		if (clazz.equals(HazelcastInstance.class)) {
			Logger.info("%s Injection...OK", clazz.getName());
			return (T) instance;
		}else if(clazz.equals(ExecutorService.class)){
			return (T) instance.getExecutorService();
		}else if(clazz.equals(Transaction.class)){
			return (T) instance.getTransaction();
		}
		Logger.info("%s Injection...KO", clazz.getName());
		return null;
	}

	/* (non-Javadoc)
	 * @see play.inject.NamedBeanSource#getBeanOfType(java.lang.Class, java.lang.String)
	 */
	public <T> T getBeanOfType(Class<T> clazz, String name) {
		if (clazz.equals(IMap.class) || clazz.equals(Map.class)) {
			return (T) instance.getMap(name);
		}else if(clazz.equals(IList.class) || clazz.equals(List.class)){
			return (T) instance.getList(name);
		}else if(clazz.equals(ExecutorService.class)){
			return (T) instance.getExecutorService(name);
		}else if(clazz.equals(ISet.class) || clazz.equals(Set.class)){
			return (T) instance.getSet(name);
		}else if(clazz.equals(IdGenerator.class)){
			return (T) instance.getIdGenerator(name);
		}else if(clazz.equals(AtomicNumber.class)){
			return (T) instance.getAtomicNumber(name);
		}else if(clazz.equals(MultiMap.class)){
			return (T) instance.getMultiMap(name);
		}else if(clazz.equals(IQueue.class) || clazz.equals(Queue.class)){
			return (T) instance.getQueue(name);
		}else if(clazz.equals(ITopic.class)){
			return (T) instance.getTopic(name);
		}
		Logger.info("%s Injection...KO", clazz.getName());
		return null;
	}

}