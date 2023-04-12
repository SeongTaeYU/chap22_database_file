package com.javalab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * [static 전역변수]
 * JDBC 프로그래밍을 위한 요소들을 모두 멤버변수 즉, 필드 위치로 뽑아올림
 *  - 본 클래스 어디서라도 사용가능한 전역변수가 됨
 *  [모듈화]
 *  - 데이터베이스 커넥션 + PreparedStatement + 쿼리실행 작업 모듈
 *  - 실제로 쿼리를 실행하고 결과를 받아오는 부분 모듈
 *  [미션]
 *   - 전체 상품의 정보를 조회하세요(카테고리명이 나오도록)
 */

public class DatabaseMain {
	//[멤버변수]
	//1. Oracle 드라이버 일므 문자열 상숭
	public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
	
	//2. Oracle 데이터베이스 접속 경로(url) 문자열 상숭
	public static final String DB_URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
	
	//3.데이터베이스 접속 객체
	public static Connection con = null;
	
	//4. query 실행 객체
	public static PreparedStatement pstmt = null;
	
	//5. select 결과 저장 객체
	public static ResultSet rs = null;
	
	//6. Oracle 계정(id/pwd)
	public static String oracleId = "tempdb";
	
	//7. Oracle Password
	public static String oraclePwd = "1234";
	
	
	public static void main(String[] args) {
		//1. 디비 접속 메소드 호출
		connectDB();
		
		//2. 쿼리문 실행 메소드 호출
		selectAllProduct();
		
		//3. 특정 카테고리 소속된 상품들만 조회하는 메소드
		// - (여기서는 커넥션 객체 자원을 반납한다.)
		String categoryName = "전자제품";
		selectProductByCategory(categoryName);
		
		//4. 가격이 25,000원 이상인 상품들의 이름과 가격을 조회하시오
		selectProductGatherThan();
		
		//5. 카테고리별로 카테고리명과 가격의 합계금액을 조회하되 금액이 큰 순서로 정렬
		selectProductGroupByCategory();
		
		//6.상품추가 : 카테고리 : 식료품 / 상품ID : 기존 번호 +1 상품명 : 양배추 / 가격 : 2000 / 입고일 : 2022/07/10
		insertProduct();
		
		//7. 상품 가격 변경(수정) 탱크로리 상품의 가격을 600000으로 수정
		updateProduct();
		
		//8. 자원반납
		closeResource();
		
		//9. 자원반납
		closeResource(pstmt,rs);
	}//end main

	//7. 상품 가격 변경(수정) 탱크로리 상품의 가격을 600000으로 수정
	private static void updateProduct() {
		try {
			int price = 600000;
			String pName = "탱크로리";
			
			String sql = "update product p set p.price = ?";
			sql += " where product_name = ?";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1,price);
			pstmt.setString(2,pName);
			
			int result = pstmt.executeUpdate();
			
			if(result > 0) {
				System.out.println("7.수정 성공");
			}else {
				System.out.println("7.수정 실패");
			}
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			//자원 해제 메소드 호출 
			closeResource(pstmt, rs);
		}
	}//end updateProduct

	//6.상품추가 : 카테고리 : 식료품 / 상품ID : 기존 번호 +1 상품명 : 양배추 / 가격 : 2000 / 입고일 : 2022/07/10
	private static void insertProduct() {
		try {
			
			int productId = 22;
			String productName = "양배추";
			int price = 2000;
			int categoryId = 5;
			String receiptDate = "20220710";
			
			String sql = "insert into product(product_id, product_name, price, category_id, receipt_date) values(?,?,?,?,to_date(?,'yyyy-mm-dd'))";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1,productId);
			pstmt.setString(2,productName);
			pstmt.setInt(3,price);
			pstmt.setInt(4,categoryId);
			pstmt.setString(5,receiptDate);
			
			int result = pstmt.executeUpdate();
			if(result> 0) {
				System.out.println("6.저장 성공");
			}else {
				System.out.println("6.저장 실패");
			}

			System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}//end insertProduct

	//5. 카테고리별로 카테고리명과 가격의 합계금액을 조회하되 금액이 큰 순서로 정렬
	private static void selectProductGroupByCategory() {
		try {
			String sql = "select c.category_id, c.category_name, sum(p.price) \"합계금액\"";
			sql += " from product p left outer join category c on p.category_id = c.category_id";
			sql += " group by c.category_id, c.category_name";
			sql += " order by \"합계금액\" desc";
			
			pstmt = con.prepareStatement(sql);
			System.out.println("5.카테고리별로 카테고리명과 가격의 합계금액을 조회 객체 생성 성공");
			
			rs = pstmt.executeQuery();
			System.out.println();
			
			while(rs.next()) {
				System.out.println(rs.getInt("category_id")+"\t"+
								   rs.getString("category_name")+"\t"+
								   rs.getInt("합계금액")
								  );
				
			}
			System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}//end selectProductGroupByCategory

	//4. 가격이 25,000원 이상인 상품들의 이름과 가격을 조회하시오
	private static void selectProductGatherThan() {
		try{
			int price = 25000;
		
		
		String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
		sql += " from category c left outer join product p on c.category_id = p.category_id";
		sql += " where p.price >= ?";
		sql += " order by p.price";
		
		
		pstmt = con.prepareStatement(sql);
		System.out.println("4. 가격 비교 객체 생성 성공");
		
		pstmt.setInt(1,price);
		rs = pstmt.executeQuery();
		System.out.println();
		
		while(rs.next()) {
			System.out.println(rs.getInt("category_id")+"\t"+
							   rs.getString("category_name")+"\t"+
							   rs.getInt("product_id")+"\t"+
							   rs.getString("product_name")+"\t"+
							   rs.getInt("price")+"\t"+
							   rs.getDate("receipt_date")
							  );
			
		}
		System.out.println();
	}catch(SQLException e) {
		System.out.println("SQL ERR : "+e.getMessage());
	}finally {
		closeResource(pstmt, rs);
	}
		
	}//end selectProductGatherThan

	//3. 특정 카테고리 소속된 상품들만 조회하는 메소드
	private static void selectProductByCategory(String categoryName) {
		try {
			//3. preparestatment 객체를 통해서 쿼리하기 위한
			//SQL 쿼리문장만들기(삽입,수정,삭제,조회)
			//...............쿼리문 구현....................
			String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
			sql += " from category c left outer join product p on c.category_id = p.category_id";
			sql += " where c.category_name = ?";
			sql += " order by c.category_id asc";
			//4.커넥션 객체를 통해서 데이터 베이스에 쿼리(SQL)를 실행해주는 preparestatment 객체 얻음
			//
			pstmt = con.prepareStatement(sql);
			System.out.println("3. 전자제품 조회 객체 생성 성공");
			
			pstmt.setString(1,categoryName);
			
			//5. Statement 객체의 excecuteQuery() 메소드를 통해서 쿼리 실행
			// 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
			rs = pstmt.executeQuery();
			System.out.println();
			
			//6. rs.next()의 의미 설명
			while(rs.next()) {
				System.out.println(rs.getInt("category_id")+"\t"+
								   rs.getString("category_name")+"\t"+
								   rs.getInt("product_id")+"\t"+
								   rs.getString("product_name")+"\t"+
								   rs.getInt("price")+"\t"+
								   rs.getDate("receipt_date")
								  );
				
			}
			System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}//end selectProductByCategory


	private static void selectAllProduct() {
		try {
			//3. preparestatment 객체를 통해서 쿼리하기 위한
			//SQL 쿼리문장만들기(삽입,수정,삭제,조회)
			//...............쿼리문 구현....................
			String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
			sql += " from category c left outer join product p on c.category_id = p.category_id";
			sql += " order by c.category_id asc, p.product_id desc";
			//4.커넥션 객체를 통해서 데이터 베이스에 쿼리(SQL)를 실행해주는 preparestatment 객체 얻음
			//
			pstmt = con.prepareStatement(sql);
			System.out.println("2.전체 상품 객체 생성 성공");
			
			//5. Statement 객체의 excecuteQuery() 메소드를 통해서 쿼리 실행
			// 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
			rs = pstmt.executeQuery();
			System.out.println();
			
			//6. rs.next()의 의미 설명
			while(rs.next()) {
				System.out.println(rs.getInt("category_id")+"\t"+
								   rs.getString("category_name")+"\t"+
								   rs.getInt("product_id")+"\t"+
								   rs.getString("product_name")+"\t"+
								   rs.getInt("price")+"\t"+
								   rs.getDate("receipt_date")
								  );
				
			}
			System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}
	}//end selectProduct

	//드라이버 로딩과 커넥션 객체 생성 메소드
	private static void connectDB() {
		try {
			//1. 드라이버 로딩
			Class.forName(DRIVER_NAME);
			System.out.println("1-1.드라이버로드 성공");
			
			//2.데이터베이스 커넥션(연결)
			con = DriverManager.getConnection(DB_URL,oracleId,oraclePwd);
			System.out.println("1-2.커넥션 객체 생성 성공\n");
			
		}catch(ClassNotFoundException e) {
			System.out.println("드라이버 로드 ERR : "+e.getMessage());
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
		
	}//end connectDB

	private static void closeResource() {
		try {
		
			if(con != null) {
				con.close();
			}
		}catch(SQLException e) {
			System.out.println("자원해제 ERR : "+e.getMessage());
		}
	
	}//end closeResource
	
	private static void closeResource(PreparedStatement pstmt2, ResultSet rs2) {
		try {
			if(rs != null) {
				rs.close();
			}
			if(pstmt != null) {
				pstmt.close();
			}
			
		}catch(SQLException e) {
			System.out.println("자원해제 ERR : "+e.getMessage());
		}
	}//end closeResource
	
}//end class 
