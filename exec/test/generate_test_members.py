#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
테스트용 회원 10,000명 생성 SQL 파일 생성 스크립트
사용법: python3 generate_test_members.py
"""

def generate_sql_file(num_members=10000):
    """테스트 회원 INSERT SQL 파일 생성"""
    
    output_file = 'test_members_insert.sql'
    
    with open(output_file, 'w', encoding='utf-8') as f:
        # 헤더 작성
        f.write("-- 테스트용 회원 {}명 생성 SQL\n".format(num_members))
        f.write("-- 생성일: {}\n".format(__import__('datetime').datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
        f.write("-- 사용법: mysql -u {username} -p {database} < test_members_insert.sql\n\n")
        
        # 기존 테스트 회원 삭제 (선택사항)
        f.write("-- 기존 테스트 회원 삭제 (선택사항, 주석 해제하여 사용)\n")
        f.write("-- DELETE FROM member WHERE social_type = 'TEST' AND social_uid LIKE 'test_uid_%';\n\n")
        
        # 100개씩 나누어서 여러 개의 INSERT 문으로 생성
        batch_size = 100
        total_batches = (num_members + batch_size - 1) // batch_size
        
        for batch_num in range(total_batches):
            batch_start = batch_num * batch_size + 1
            batch_end = min((batch_num + 1) * batch_size, num_members)
            
            # 각 배치마다 독립적인 INSERT 문 작성
            f.write(f"-- 배치 {batch_num + 1}/{total_batches} ({batch_start}~{batch_end}번 회원)\n")
            f.write("INSERT INTO member (social_uid, email, social_type, nickname, is_deleted, created_at, updated_at) VALUES\n")
            
            # VALUES 생성
            values = []
            for i in range(batch_start, batch_end + 1):
                value = f"('test_uid_{i}', 'test{i}@test.com', 'TEST', '테스트유저{i}', 0, NOW(6), NOW(6))"
                values.append(value)
            
            # 마지막 값이 아니면 콤마, 마지막 값이면 세미콜론
            f.write(',\n'.join(values) + ';\n\n')
        
        f.write("\n-- 총 {}명의 테스트 회원이 생성되었습니다.\n".format(num_members))
        f.write("-- 생성된 회원 ID 확인: SELECT member_id, social_uid, nickname FROM member WHERE social_type = 'TEST' LIMIT 10;\n")
    
    print(f"✅ SQL 파일 생성 완료: {output_file}")
    print(f"   총 {num_members}명의 테스트 회원 INSERT 문이 생성되었습니다.")
    print(f"\n📝 사용 방법:")
    print(f"   mysql -u [username] -p [database_name] < {output_file}")

if __name__ == '__main__':
    generate_sql_file(10000)

