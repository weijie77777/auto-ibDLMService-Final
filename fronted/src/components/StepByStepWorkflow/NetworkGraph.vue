<!-- NetworkGraph.vue -->
<template>
  <!-- <h1>{{ props.data.taskId}}</h1> -->
  <div ref="container" class="graphcontent"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Network } from 'vis-network/standalone'

// props获得父组件传递的数据
const props = defineProps(['data'])
// 获得容器元素
const container = ref(null)
let network = null

// 将父组件传递的数据进行一个转换
const transformData = (data) => {
  // 如果 data 不存在、edges 不存在、或 edges 为空数组，直接返回空
  if (!data?.edges?.length) return { nodes: [], edges: [] }

  const MAX_EDGES = 100

  // ========== 1. 找到最有影响力的节点（度最大的节点） ==========
  const degreeMap = calculateNodeDegrees(data.edges)
  const mostInfluentialNodeId = getMostInfluentialNode(degreeMap)
  if (!mostInfluentialNodeId) return { nodes: [], edges: [] }

  // ========== 2. 从核心节点出发BFS获取最多100条边 ==========
  const selectedEdges = bfsGetEdges(data.edges, mostInfluentialNodeId, MAX_EDGES)

  // ========== 3. 从边中提取所有节点ID ==========
  const nodeIdSet = new Set()
  selectedEdges.forEach(e => {
    nodeIdSet.add(e.source)
    nodeIdSet.add(e.target)
  })

  // ========== 4. 构建节点（从idMapping获取标签）==========
  const nodes = Array.from(nodeIdSet).map(id => ({
    id: id,
    label: data.idMapping?.[id] || `user_${id}`,  // 兜底显示 通过idMapping获得用户的原始id
    title: `ID: ${id}, User_id: ${data.idMapping?.[id] || 'unknown'}`,  // 悬停提示 给出用户id以及映射后的id
    color: id === mostInfluentialNodeId ? '#e74c3c' : '#3498db', // 核心节点标红
    size: id === mostInfluentialNodeId ? 30 : 20 // 核心节点更大
  }))

  // ========== 5. 构建边（不同类型不同颜色 + 显示时间）==========
  const edges = selectedEdges.map((e, i) => ({
    id: i,
    from: e.source,
    to: e.target,
    label: `${e.type}\n${e.timestamp?.slice(0, 16) || ''}`,  // 显示类型和时间 (YY-MM-DD HH:mm)
    title: `Type: ${e.type}, Create_time: ${e.timestamp?.slice(0, 16) || 'unknown'}`,  // 悬停详情
    color: getEdgeColor(e.type),
    width: 2,
    arrows: 'to',
    smooth: { type: 'continuous' }
  }))

  console.log(`构图: 核心节点${mostInfluentialNodeId}, ${nodes.length} 节点, ${edges.length} 边`)
  return { nodes, edges }
}

// 计算每个节点的度（入度+出度）- 核心：判定影响力
const calculateNodeDegrees = (edges) => {
  const degreeMap = new Map()
  edges.forEach(e => {
    // 出度：source节点+1
    degreeMap.set(e.source, (degreeMap.get(e.source) || 0) + 1)
    // 入度：target节点+1
    degreeMap.set(e.target, (degreeMap.get(e.target) || 0) + 1)
  })
  return degreeMap
}

// 找到度最大的节点（最有影响力）
const getMostInfluentialNode = (degreeMap) => {
  let maxDegree = -1
  let coreNodeId = null
  for (const [nodeId, degree] of degreeMap.entries()) {
    if (degree > maxDegree) {
      maxDegree = degree
      coreNodeId = nodeId
    }
  }
  return coreNodeId
}

// BFS遍历获取最多maxCount条边
const bfsGetEdges = (allEdges, startNodeId, maxCount) => {
  // 构建邻接表：key=节点ID，value=该节点关联的所有边
  const adjacencyList = buildAdjacencyList(allEdges)
  // 已访问的边（避免重复）和已访问的节点（BFS遍历用）
  const visitedEdges = new Set()
  const visitedNodes = new Set()
  // BFS队列：存储待遍历的节点ID
  const queue = [startNodeId]
  visitedNodes.add(startNodeId)
  
  const selectedEdges = []

  while (queue.length > 0 && selectedEdges.length < maxCount) {
    const currentNode = queue.shift()
    // 获取当前节点的所有关联边
    const currentEdges = adjacencyList.get(currentNode) || []
    
    for (const edge of currentEdges) {
      // 避免重复添加同一条边（用source+target+type+timestamp做唯一标识）
      const edgeKey = `${edge.source}_${edge.target}_${edge.type}_${edge.timestamp}`
      if (!visitedEdges.has(edgeKey) && selectedEdges.length < maxCount) {
        visitedEdges.add(edgeKey)
        selectedEdges.push(edge)
        
        // 将边的另一端节点加入队列（未访问过的话）
        const neighborNode = edge.source === currentNode ? edge.target : edge.source
        if (!visitedNodes.has(neighborNode)) {
          visitedNodes.add(neighborNode)
          queue.push(neighborNode)
        }
      }
    }
  }

  return selectedEdges
}

// 构建邻接表：方便快速查找节点的所有关联边
const buildAdjacencyList = (edges) => {
  const adjacencyList = new Map()
  edges.forEach(edge => {
    // 给source节点添加边
    if (!adjacencyList.has(edge.source)) {
      adjacencyList.set(edge.source, [])
    }
    adjacencyList.get(edge.source).push(edge)
    
    // 给target节点也添加边（无向遍历，BFS能覆盖双向关联）
    if (!adjacencyList.has(edge.target)) {
      adjacencyList.set(edge.target, [])
    }
    adjacencyList.get(edge.target).push(edge)
  })
  return adjacencyList
}

// 边颜色映射
const getEdgeColor = (type) => {
  const colors = {
    repost: '#27ae60',   // 转发 - 绿色
    comment: '#e67e22',  // 评论 - 橙色
    thumb: '#9b59b6',    // 点赞 - 紫色 默认没有使用点赞行为
    original: '#3498db'  // 原创 - 蓝色
  }
  return colors[type] || '#95a5a6'  // 默认灰色
}

// 构建网络
const draw = () => {
  const { nodes, edges } = transformData(props.data)
  
  if (!network) {
    network = new Network(container.value, { nodes, edges }, {
      nodes: { 
        shape: 'dot', 
        size: 25, 
        font: { size: 14 } 
      },
      edges: { 
        arrows: 'to', 
        smooth: true 
      },
      physics: { 
        enabled: true,
        // 调整物理参数让图更紧凑
        barnesHut: {
          gravitationalConstant: -3000,  // 引力常数（负值越大，节点越分散）
          centralGravity: 0.3,            // 中心引力
          springLength: 100,              // 弹簧长度（边长）
          springConstant: 0.04,
          damping: 0.09,
          avoidOverlap: 0.5               // 避免重叠
        },
        stabilization: {
          enabled: true,
          iterations: 200,    // 稳定化迭代次数
          updateInterval: 25
        }
      },
      layout: {
        // 随机种子，保证可重复
        randomSeed: 2
      },
      interaction: {
        hover: true,
        tooltipDelay: 200,
        // 允许缩放和平移
        zoomView: true,
        dragView: true
      }
    })
    
    // ========== 关键：自适应容器大小 ==========
    network.once('stabilizationIterationsDone', () => {
      network.fit({         // 自适应视图
        animation: {
          duration: 1000,
          easingFunction: 'easeInOutQuad'
        }
      })
    })
    
  } else {
    network.setData({ nodes, edges })
    network.fit()  // 数据更新后重新适应
  }
}

// 监听数据变化重新绘图
watch(() => props.data, draw, { deep: true })

// 挂载时绘图
onMounted(() => {
  if (container.value) {
    draw()
  }
})
onMounted(draw)
watch(() => props.data, draw, { deep: true })
</script>
<style scoped>
  .graphcontent{
    width: 100%;
    height: 400px;
    /* 浅灰背景 */
    background: #f8fafc;
    border-radius: 12px;
  }
  .graphcontent::before{
    content: '';
    position: absolute;
    inset: 0;
    background-size: 50px 50px;
    animation: gridMove 20s linear infinite;
    pointer-events: none;
  }
</style>
