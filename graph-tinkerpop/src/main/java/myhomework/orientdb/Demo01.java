package myhomework.orientdb;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo01 {
	
	private static final String orientDbUrl = "remote:10.10.81.103/demodb";
	
	private static final String orientDbUser = "root";
	
	private static final String orientDbPwd = "qware123";
	

	// 测试连接gremlin - OrientDB方案
	public void connectGremlin() throws Exception {
		OrientGraphFactory factory = new OrientGraphFactory(orientDbUrl, orientDbUser, orientDbPwd);
		// 默认端口2424
		Graph graph = factory.getNoTx();

		log.debug("graph.features {}", graph.features().toString());

		GraphTraversalSource g = graph.traversal();
		
		
		//variables 变量
		if(graph.features().graph().variables().supportsVariables()) {
			//xxx
			log.debug("++++=+++++++++++++====");
			log.debug("graph.variables:");
			graph.variables().asMap().forEach( (p,v) -> log.debug("{}:{}",p,v));
			log.debug("++++=+++++++++++++====");
		}else {
			log.debug(" graph.variables not support !!");
		}
		
		// 計算有多少頂點
		log.debug("vertices Count:{}", g.V().count().next());
		// 計算有多少邊
		log.debug("edges count: {}", g.E().count().next());
		//
		// log.debug("{}",g.V().out("HasFriend").values().next());
		// 計算有多少被标签为 Profiles 的 頂點
		log.debug("計算有多少被标签为  Profiles 的 頂點:{}", g.V().hasLabel("Profiles").count().next());

		// 查找所有name = Santo 的Profiles
		log.debug("查找所有name = Santo 的Profiles :{}", g.V().hasLabel("Profiles").has("Name", "Santo").count().next());

		// 查找所有name = Santo 的Profiles
		log.debug("Profiles(Name = Santo) vertices Count:");
		g.V().hasLabel("Profiles").has("Name", "Santo").toStream().forEach(xx -> log.debug("{}", printPros(xx)));

		// 計算 HasFriend 有多少邊
		log.debug("edges HasFriend count: {}", g.E().hasLabel("HasFriend").count().next());

		Object santo_id = g.V().hasLabel("Profiles").has("Name", "Santo").id().next();
		
		// 获取 Santo 的数据
		GraphTraversal<Vertex, Vertex> v41_1 = g.V(santo_id).as("get Santo");
		log.debug("获取 Santo 的数据");
		v41_1.toStream().forEach(xx -> log.debug("{}", printPros(xx)));

		// 获取 Santo 的 外连 friend
		GraphTraversal<Vertex, Vertex> v41_1_out_f = g.V(santo_id).out("HasFriend");
		log.debug("获取 Santo 的 外连 friend 共计");
		log.debug("{}", v41_1_out_f.count().next());

		log.debug("获取 Santo 的 外连 friend -- 查找 Email 为 sergey的");
		g.V(santo_id)
			.out("HasFriend")
			.filter(xx -> StringUtils.startsWith(xx.get().value("Email"), "sergey"))
			.toStream()
			.forEach(xx -> log.debug("{}", printPros(xx)));

		log.debug("获取 Santo 的 外连 friend 列表");
		g.V(santo_id)
			.out("HasFriend")
			.toStream()
			.forEach(xx -> log.debug("{}", printPros(xx)));

		// 获取 Santo 的内连 friend
		GraphTraversal<Vertex, Vertex> v41_1_in_f = g.V(santo_id).in("HasFriend");
		log.debug("{}", v41_1_in_f.count().next());
		g.V(santo_id).in("HasFriend").toStream().forEach(xx -> log.debug("{}", printPros(xx)));
		// graph.vertices().

		log.debug("获取 Santo 的 外连 friend repeat 2列表");
		// g.V(santo_id).as("get_1").repeat(g.V().hasLabel("Profiles").out("HasFriend")).times(3).toStream().forEach(xx
		// -> log.debug("{}", printPros(xx)));
		
	}
	
	/**
	 * mapStep
	 * @param g
	 */
	public void mapStep(GraphTraversalSource g) {
		//获取对应 santo id 
		Object santo_id = g.V().hasLabel("Profiles").has("Name", "Santo").id().next();
		log.debug("======Name:Santo Profiles {} ========",santo_id);
		//map - 转换为对象
		log.debug("HasFriend map Name:-----------");
		g.V(santo_id).in("HasFriend")
			//返回任意对象
			.map(v -> v.get().value("Name"))
			.toStream()
			.forEach(p -> log.debug("{}",p));
		
		log.debug("HasFriend map Name:-----------");
		
		g.V(santo_id).in("HasFriend")
		//返回任意对象
		.map(__.values("Name"))
		.toStream()
		.forEach(p -> log.debug("{}",p));
		log.debug("HasFriend :+++++++++++");
	}
	
	/**
	 * flatMap - 转换为 iterator
	 * @param g
	 */
	public void flatMapStep(GraphTraversalSource g) {
		//获取对应 santo id 
		Object santo_id = g.V().hasLabel("Profiles").has("Name", "Santo").id().next();
		log.debug("======Name:Santo Profiles {} ========",santo_id);
		// flatMap - 转换为 iterator
		log.debug("HasFriend flatMap Name:-----------");
		g.V(santo_id).in("HasFriend")
			//必须返回Iterator
			.flatMap(v -> v.get().values("Name","Email"))
			.toStream()
			.forEach(p -> log.debug("{}", p));
		log.debug("HasFriend :+++++++++++");
	}
	
	/**
	 * filter -  过滤
	 * @param g
	 */
	public void filter(GraphTraversalSource g) {
		log.debug("filter :-----------");
		
		log.debug("--------------------  filter label -----------");
		g.V().filter(p->"Profiles".equals(p.get().label())).limit(10)
			.toStream()
			.forEach(xx -> log.debug("{}", printPros(xx)));
		
		log.debug("--------------------  filter label 2-----------");
		g.V().filter(__.label().is("Profiles")).limit(10)
			.toStream()
			.forEach(xx -> log.debug("{}", printPros(xx)));
		
		log.debug("--------------------  filter property -----------");
		g.V()
			.filter(p-> p.get().property("Bio").isPresent() && "OrientDB Team".equals(p.get().property("Bio").value()))
			.limit(10)
		.toStream()
		.forEach(xx -> log.debug("{}", printPros(xx)));
	
		log.debug("filter :+++++++++++");
	
	}
	
	/**
	 * sideEffect 遍历及进行相关操作，进行相关传递
	 * @param g
	 */
	public void sideEffect(GraphTraversalSource g) {
		// sideEffect 遍历及进行相关操作，进行相关传递
		log.debug("sideEffect :----------------------------------------");
		//sideEffect
		log.debug("{}",g.V()
			.hasLabel("Profiles")
			.count().next());
			//.sideEffect(System.out::print);
		log.debug("sideEffect forEachRemaining:----------------------------------------");
		g.V()
			.hasLabel("Profiles").limit(10)
			.sideEffect(p->p.get().property("key", UUID.randomUUID().toString()))
			.toStream().forEach(action->log.debug("{} keys{} key {}",action,action.keys(),action.property("key")));
		
		
		log.debug("sideEffect inE outE:----------------------------------------");
		
		//计算出入度
		g.V().hasLabel("Profiles").has("Bio", "OrientDB Team")
				.sideEffect(__.store("o_n").outE().count().store("o"))
				.sideEffect(__.store("i_n").inE().count().store("i"))
				.cap("o_n","o","i_n","i")
				.toStream().forEach(p -> log.debug("{}",p));
		
		log.debug("sideEffect : ++++++++++++++++++++++++++++++++++++++++");
	}
	
	
	/**
	 * branch 分支，可以使用 choose
	 * @param g
	 */
	public void branch(GraphTraversalSource g) {
		//branch 分支，使用 option 进行条件
//		
		
		log.debug("branch - option : ------------------------------------");
		log.debug("count: {}",
				g.V()
				.hasLabel("Profiles")
//				.hasValue("Bio")
				.limit(100)
				.branch(it->it.get().value("Gender"))
				.option("Male", __.values("Name","Email"))
				.option("Female", __.values("Name","Email","Birthday"))
				.count().next());
		
		
		g.V()
		.hasLabel("Profiles")
//		.hasValue("Bio")
		.limit(100)
		.branch(it->it.get().value("Gender"))
		.option("Male", __.values("Name","Email","Gender"))
		.option("Female", __.values("Name","Email","Gender","Birthday"))
		.toStream().forEach(p -> log.debug("{}",p));
	
		log.debug("=========== branch - option : ====================");
		
		log.debug("count: {}",
				g.V().hasLabel("Profiles").limit(100)
				.branch(it->it.get().value("Bio"))
				.option("OrientDB Team", __.values("Name","Email"))
				.option(__.values("Name","Bio")).count().next());
		

		g.V().hasLabel("Profiles").limit(100)
				.branch(it->it.get().value("Bio"))
				.option("OrientDB Team", __.values("Name","Email","Bio"))
				.option(__.values("Name","Bio"))
				.toStream().forEach(p ->  log.debug("{}",p));
		
		log.debug("=========== choose : ====================");
		
		log.debug("count: {}",
				g.V().hasLabel("Profiles").limit(100)
				.choose(__.has("Bio","OrientDB Team"), 
						__.values("Name","Email"),
						__.values("Name","Bio"))
				.count().next());

		g.V().hasLabel("Profiles").limit(100)
				.choose(__.has("Bio","OrientDB Team"), 
						__.values("Name","Email","Bio"),
						__.values("Name","Bio"))
				.toStream().forEach(p ->  log.debug("{}",p));
	
		log.debug("branch - option : +++++++++++++++++++++++++");
	}
	
	/**
	 * Terminal Steps
	 */
	public void terminalSteps() {
		
		OrientGraphFactory factory = new OrientGraphFactory(orientDbUrl, orientDbUser, orientDbPwd);
		// 默认端口2424
		Graph graph = factory.getNoTx();

		GraphTraversalSource g = graph.traversal();
		
		Object santo_id = g.V().hasLabel("Profiles").has("Name", "Santo").id().next();
		log.debug("======Name:Santo Profiles {} ========",santo_id);
		
		Object luigi_id = g.V().hasLabel("Profiles").has("Name", "Luigi").id().next();
		log.debug("======luigi_id Profiles {} ========",luigi_id);
		
//		hasNext() 确定是否有可用的结果。
		log.debug(" santo hasFriend hasNext: {}",g.V(santo_id).out("HasFriend").hasNext());
//		next() 将返回下一个结果。 only 1 data
		log.debug(" santo hasFriend next: {}",g.V(santo_id).out("HasFriend").next());

//		next(n)将n在列表中返回下一个结果。
		log.debug(" santo hasFriend next(2): {}",g.V(santo_id).out("HasFriend").next(2));

//		tryNext()将返回一个Optional,是hasNext()/next()的复合。
		log.debug(" santo hasFriend tryNext: {}",g.V(santo_id).out("HasFriend").tryNext());
		log.debug(" santo HasEated tryNext: {}",g.V(santo_id).out("HasEated").tryNext());
		
		Collection<Vertex> result;
//		toList() 将返回列表中的所有结果。
		result = g.V(santo_id).both("HasFriend").toList();
		log.debug(" santo both hasFriend toList: {} data {}",result.size(),result);

//		toSet() 将返回一组中的所有结果（因此，重复删除）。
		result= g.V(santo_id).both("HasFriend").toSet();
		log.debug(" santo both hasFriend toSet: {}  data {}",result.size(),result);

//		toBulkSet() 将返回加权集中的所有结果（因此，通过加权保留重复项）。
		result= g.V(santo_id).both("HasFriend").toBulkSet();
		log.debug(" santo both hasFriend toBulkSet: {}  data {}",result.size(),result);


//		fill(collection) 将所有结果放入提供的集合中，并在完成后返回集合。
		log.debug(" santo,luigi both hasFriend: {}", g.V(luigi_id).both("HasFriend").fill(result));
		

//		iterate() 并不完全符合终止步骤的定义，因为它不返回结果，但仍然返回遍历 - 但它确实表现为终止步骤，因为它迭代遍历并生成副作用而不返回实际结果。
		log.debug("luigi both hasFriend: {}",g.V(luigi_id).both("HasFriend").iterate());
		
		factory.close();
	}
	
	/**
	 * 一系列的 通用步骤
	 * @throws Exception
	 */
	public void generalStep() throws Exception {
		OrientGraphFactory factory = new OrientGraphFactory(orientDbUrl, orientDbUser, orientDbPwd);
		// 默认端口2424
		Graph graph = factory.getNoTx();

		GraphTraversalSource g = graph.traversal();
		
//		
//		
		log.debug("======================");
		log.debug("General Step:");
		
		
		mapStep(g);
		flatMapStep(g);
		filter(g);
		sideEffect(g);
		branch(g);
//		
//		
//		g.V().hasLabel("Profiles").limit(10)
//			.branch(it->it.get().property("Bio"))
//			.option("OrientDB Team", __.values("Name","Email"))
//			.option(null, __.values("Name")).toStream().forEach(p -> log.debug("{}",p));
//		
//		
		// graph.a
		factory.close();
		// long profileCount =
		// IteratorUtils.count(graph.getVertices("class","Profiles"));
		// System.out.println("profileCount:"+profileCount);

	}

	/**
	 * 聚合
	 */
	public void aggregateStep() {
		
		log.debug("----------------------aggregateStep-------------------------");
		
		OrientGraphFactory factory = new OrientGraphFactory(orientDbUrl, orientDbUser, orientDbPwd);
		// 默认端口2424
		Graph graph = factory.getNoTx();

		GraphTraversalSource g = graph.traversal();
		
		Object santo_id = g.V().hasLabel("Profiles").has("Name", "Santo").id().next();
		
//		g.V(santo_id).out("hasFriend")
//			.aggregate("x").in("hasFriend")
//			.out("hasFriend").where(P.without("x"))
//			.toStream().forEach(p ->  log.debug("{}",p));;
		
		
//		log.debug("count in {}",g.V("#41:0").out("HasFriend").count().next());
//		
//		log.debug("count out {}",g.V("#41:0").in("HasFriend").count().next());
		
//		GraphTraversal g1 = g.V(g.V().hasLabel("Profiles").has("Name", "Colin").id().next()).out("HasFriend");
//		log.debug("explain {}",g1.explain().prettyPrint());
//		log.debug("count {}",g1.count().next());
		log.debug("+++++++++++++++++++++++aggregateStep-------------- +++++++++++++++++++++++++++++");
		
		log.debug("santo eaten at ");
		g.V(santo_id).as("santo").in("HasProfile").out("HasEaten").toSet().forEach(p ->  log.debug("{} p: {}",p,printPros(p)));
		
		log.debug("===============");
		log.debug("santo' friends eaten at ,but santo not:");
		//查找 santo 作为 customer eaten的 XX，聚合为 x
		g.V(santo_id).as("santo").in("HasProfile").out("HasEaten").aggregate("x")
			//查找
			.select("santo").both("HasFriend").in("HasProfile").out("HasEaten")
			//进行去除 santo eaten
			.where(P.without("x"))
			.toSet().forEach(p ->  log.debug("{} p: {}",p,printPros(p)));
			
//		log.debug("+++++++++++++++++++++++aggregateStep groupCount-------------- +++++++++++++++++++++++++++++");
//			g.V(santo_id).as("santo").in("HasProfile").out("HasEaten")
//			.aggregate("x").in("HasEaten")
//			.out("HasProfile").in("HasProfile").out("HasEaten")
//			.where(P.without("x"))
//			.values("Type")
//			.groupCount()
//			.toStream().forEach(p ->  log.debug("{}",p));
//		
		log.debug("+++++++++++++++++++++++aggregateStep+++++++++++++++++++++++++++++");
		Object samuel_id = g.V().hasLabel("Profiles").has("Name", "Samuel").id().next();
		
		g.V(samuel_id).out("HasFriend")
		.aggregate("x").in("HasFriend")
		.out("HasFriend").where(P.without("x")).toStream().forEach(p ->  log.debug("{} p:{}",p,printPros(p)));
	
		log.debug("=======================aggregateStep==========================");
		
		factory.close();
	}
	
	public void pathStep() {
		

		log.debug("----------------------aggregateStep-------------------------");
		
		OrientGraphFactory factory = new OrientGraphFactory(orientDbUrl, orientDbUser, orientDbPwd);
		// 默认端口2424
		Graph graph = factory.getNoTx();

		GraphTraversalSource g = graph.traversal();
		
		Object santo_id = g.V().hasLabel("Profiles").has("Name", "Santo").id().next();
		
		g.V(santo_id).out("HasFriend")
			.repeat(__.out("HasFriend"))
			.times(10)
//			.until(__.hasValue("Colin"))
			.path().by("Name")//.by("From")
			.toSet().forEach(p->log.debug("path:{}",p.toString()));
		
		factory.close();
	}

	/**
	 * 
	 * gremlin 相关增删改及事务
	 * 
	 * @startuml 
	 * auckland -> whangarei: 158 
	 * whangarei -> kaikohe: 85 
	 * kaikohe -> kaitaia: 82 
	 * kaitaia -> capeReinga: 111 
	 * whangarei -> kerikeri: 85
	 * kerikeri -> kaitaia: 88 
	 * auckland -> dargaville:175
	 * dargaville -> kaikohe: 77
	 * kaikohe -> kerikeri: 36
	 * @enduml
	 * 
	 */
	public void testTx() {

		try {
			OrientGraphFactory factory = new OrientGraphFactory(orientDbUrl, orientDbUser, orientDbPwd);
			// 默认端口2424
			Graph graph = factory.getTx();

			//
			GraphTraversalSource g = graph.traversal();
//			//注意事务开启
//			g.tx().open();
//			Vertex auckland = g.V().hasLabel("Location").has("Name","Auckland").limit(1).next();
//			if(auckland==null) {
//				auckland = graph.addVertex(T.label, "Location", "Name", "Auckland");
//			}
//			Vertex whangarei = graph.addVertex(T.label, "Location", "Name", "Whangarei");
//			Vertex dargaville = graph.addVertex(T.label, "Location", "Name", "Dargaville");
//			Vertex kaikohe = graph.addVertex(T.label, "Location", "Name", "Kaikohe");
//			Vertex kerikeri = graph.addVertex(T.label, "Location", "Name", "Kerikeri");
//			Vertex kaitaia = graph.addVertex(T.label, "Location", "Name", "Kaitaia");
//			Vertex capeReinga = graph.addVertex(T.label, "Location", "Name", "Cape Reinga");
//
//			log.debug("auckland: {}", auckland);
//			log.debug("whangarei: {}", whangarei);
//			log.debug("dargaville: {}", dargaville);
//			log.debug("kaikohe: {}", kaikohe);
//			log.debug("kerikeri: {}", kerikeri);
//			log.debug("kaitaia: {}", kaitaia);
//			log.debug("capeReinga: {}", capeReinga);
//
//			log.debug("auckland -> whangarei:{}", auckland.addEdge("Road", whangarei, "Distance", 158));
//			log.debug("whangarei -> kaikohe: {}", whangarei.addEdge("Road", kaikohe, "Distance", 85));
//			log.debug("kaikohe -> kaitaia: {}", kaikohe.addEdge("Road", kaitaia, "Distance", 82));
//			log.debug("kaitaia -> capeReinga: {}", kaitaia.addEdge("Road", capeReinga, "Distance", 111));
//			log.debug("whangarei -> kerikeri: {}", whangarei.addEdge("Road", kerikeri, "Distance", 85));
//			log.debug("kerikeri -> kaitaia: {}", kerikeri.addEdge("Road", kaitaia, "Distance", 88));
//			log.debug("auckland -> dargaville: {}", auckland.addEdge("Road", dargaville, "Distance", 175));
//			log.debug("dargaville -> kaikohe:{}", dargaville.addEdge("Road", kaikohe, "Distance", 77));
//			log.debug("kaikohe -> kerikeri:{}", kaikohe.addEdge("Road", kerikeri, "Distance", 36));
//
//			g.V(auckland).repeat(__.out().simplePath()).until(__.hasId(capeReinga)).path().by("Name").limit(1)
//					.toStream().forEach(p -> log.debug("shortestPath from auckland to capeReinga: {}", p.toString()));
//			
//			//注意事务完成后进行提交
//			g.tx().commit();
//			
//			//测试修改属性
//			g.tx().open();
//			kaitaia.property("Nation", "New Zealand");
//			//增加测试节点，预备删除等
//			Vertex unknow = graph.addVertex(T.label, "Location", "Name", "UNKNOW");
//			Edge edge = unknow.addEdge("Road", kerikeri, "Distance","to far to see!");
//			g.tx().commit();
//			
//			//删除节点、边
//			g.tx().open();
//			edge.remove();
////			unknow.remove();
//			g.tx().commit();
			//
			
			g.tx().open();
			Vertex auckland2;
			if(g.V().hasLabel("Location").has("Name","Auckland2").hasNext()) {
				auckland2 = g.V().hasLabel("Location").has("Name","Auckland2").limit(1).next();
				log.debug("found Auckland2: {}",auckland2);
			}
			else{
				auckland2 = graph.addVertex(T.label, "Location", "Name", "Auckland2");
				log.debug("add Auckland2:{} ",auckland2);
			}
			Vertex auckland3;
			if(g.V().hasLabel("Location").has("Name","auckland3").hasNext()) {
				auckland3 = g.V().hasLabel("Location").has("Name","Auckland2").limit(1).next();
				log.debug("found auckland3: {}",auckland2);
			}
			else{
				auckland3 = graph.addVertex(T.label, "Location", "Name", "Auckland2");
				log.debug("add auckland3:{} ",auckland3);
			}
			Edge edge = auckland2.addEdge("Road", auckland3,"Distance","to far to see!");
			g.tx().commit();
			
			g.tx().open();
			if(auckland2!=null) auckland2.remove();
			g.tx().commit();
			
			graph.close();
			factory.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	
//	public void testGraph() {
//		
//		OrientGraphFactory factory = new OrientGraphFactory(orientDbUrl, orientDbUser, orientDbPwd);
//		// 默认端口2424
//		Graph graph = factory.getTx();
//
//		//
////		GraphTraversalSource g = new GraphTraversalSource(graph,TraversalStrategies.);
//
//	}

	/**
	 * 打印 节点属性
	 * 
	 * @param v
	 * @return
	 */
	public static String printPros(Vertex v) {
		return v.keys().stream().map(x -> x = x + " : " + v.value(x)).collect(Collectors.joining(",  "));
	}
	
//	/***
//	 * 以下以投资路径来查找某节点的关系网络
//	 * @param moduleId
//	 */
//	public String testData(String moduleId) {
//		
//		/**
//		* TODO 通过对应 moduleId 来获取相关配置
//		*/
//		//其实查找的节点类型,通过模型配置来"股东人员表"--- INVIST_LIST,"社保缴纳记录表" --SOC_PAYMENT_LIST
//		P<String> startLables= P.within("INVIST_LIST","SOC_PAYMENT_LIST"); 
//		//前台展示给用户的为显示为名字 -实际库 属性名 为Name，需要进行转换
//		String startNodePropName = "Name";
//		//姓名，用户输入参数
//		P<String> startNodePropValues =  P.within("张三");
//
//		//投资 对应的边 实际 库 中为 HasInvist
//		String edgeLabel = "HasInvist";
//		
//		//最大查找几层，用户输入参数
//		int times = 5;
//		//终止节点类型,企业表  COMP_LIST
//		String endLables= "COMP_LIST"; 
//		//终止节点属性
//		String endNodePropName ="CompName";
//		//用户输入参数,即终止节点条件值
//		String endNodePropValue ="城云科技"; 
//		//***********
//		Graph graph = null;//根据系统参数获取 Graph
//		GraphTraversalSource g = graph.traversal();
//		
//		
//		Object santo_id = g.V().hasLabel(startLables).has(startNodePropName,startNodePropValues).id().next();
//
//		// 获取 Santo 的 外连 friend
//		log.debug("以投资路径来查找某节点的关系网络 --{}",moduleId);
//		StringBuffer result = new StringBuffer();
//		g.V(santo_id)
//			.out(edgeLabel)
//			.repeat(__.out(edgeLabel))
//			.times(times)
//			.filter(__.hasLabel(endLables).has(endNodePropName,endNodePropValue))
//			.toStream()
//			.forEach(xx -> {
//				log.debug("输出给前端。。。。。。。。。。");
//				result.append(xx).append("||||");
//			});
//		/****
//		 * TODO 一系列转换操作，权限过滤等等 
//		 */
//		
//		return result.toString();
//		
//	}

	public static void main(String[] args) {
		try {
			Demo01 demo = new Demo01();
//			demo.connectGremlin();
//			demo.testGraphAlgoFactory();
//			demo.generalStep();
//			demo.terminalSteps();
//			demo.aggregateStep();
			demo.pathStep();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
